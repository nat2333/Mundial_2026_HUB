import React, { useState, useEffect, useRef } from 'react';
import ReactCountryFlag from 'react-country-flag';
import { obtenerPreferencias, editarPreferencias, guardarPreferencias } from '../services/authService';
import { getAlbumUsuario } from '../services/albumService';
import { getAgendaUsuario } from '../services/perfilService';
import { getPartidoPorId } from '../services/partidoService';
import { getAllSedes } from '../services/sedesService';
import { teamIso2 } from '../utils/countries';
import '../App.css';
import Navbar from '../components/layout/Navbar';
import Footer from '../components/layout/Footer';
import Icon from '../components/ui/Icon';
import TeamFlag from '../components/TeamFlag';
import useDarkMode from '../hooks/useDarkMode';
import { useToast } from '../context/ToastContext';

// ===== Equipos del Mundial 2026 para autocompletado =====
const EQUIPOS_MUNDIAL = [
  // CONMEBOL
  { nombre: 'Argentina',          code: 'AR' },
  { nombre: 'Brasil',             code: 'BR' },
  { nombre: 'Colombia',           code: 'CO' },
  { nombre: 'Uruguay',            code: 'UY' },
  { nombre: 'Ecuador',            code: 'EC' },
  { nombre: 'Venezuela',          code: 'VE' },
  { nombre: 'Paraguay',           code: 'PY' },
  { nombre: 'Bolivia',            code: 'BO' },
  { nombre: 'Perú',               code: 'PE' },
  { nombre: 'Chile',              code: 'CL' },
  // CONCACAF
  { nombre: 'México',             code: 'MX' },
  { nombre: 'Estados Unidos',     code: 'US' },
  { nombre: 'Canadá',             code: 'CA' },
  { nombre: 'Honduras',           code: 'HN' },
  { nombre: 'Costa Rica',         code: 'CR' },
  { nombre: 'Panamá',             code: 'PA' },
  { nombre: 'Jamaica',            code: 'JM' },
  { nombre: 'El Salvador',        code: 'SV' },
  { nombre: 'Guatemala',          code: 'GT' },
  { nombre: 'Trinidad y Tobago',  code: 'TT' },
  // UEFA
  { nombre: 'España',             code: 'ES' },
  { nombre: 'Francia',            code: 'FR' },
  { nombre: 'Alemania',           code: 'DE' },
  { nombre: 'Portugal',           code: 'PT' },
  { nombre: 'Inglaterra',         code: 'GB' },
  { nombre: 'Italia',             code: 'IT' },
  { nombre: 'Países Bajos',       code: 'NL' },
  { nombre: 'Bélgica',            code: 'BE' },
  { nombre: 'Turquía',            code: 'TR' },
  { nombre: 'Dinamarca',          code: 'DK' },
  { nombre: 'Austria',            code: 'AT' },
  { nombre: 'Escocia',            code: 'GB' },
  { nombre: 'Croacia',            code: 'HR' },
  { nombre: 'Serbia',             code: 'RS' },
  { nombre: 'Hungría',            code: 'HU' },
  { nombre: 'Suiza',              code: 'CH' },
  { nombre: 'Eslovaquia',         code: 'SK' },
  { nombre: 'Rumania',            code: 'RO' },
  { nombre: 'Albania',            code: 'AL' },
  { nombre: 'Noruega',            code: 'NO' },
  { nombre: 'Polonia',            code: 'PL' },
  { nombre: 'Eslovenia',          code: 'SI' },
  { nombre: 'República Checa',    code: 'CZ' },
  { nombre: 'Grecia',             code: 'GR' },
  { nombre: 'Ucrania',            code: 'UA' },
  // AFC
  { nombre: 'Japón',              code: 'JP' },
  { nombre: 'Corea del Sur',      code: 'KR' },
  { nombre: 'Irán',               code: 'IR' },
  { nombre: 'Australia',          code: 'AU' },
  { nombre: 'Indonesia',          code: 'ID' },
  { nombre: 'Arabia Saudita',     code: 'SA' },
  { nombre: 'Jordania',           code: 'JO' },
  { nombre: 'Iraq',               code: 'IQ' },
  { nombre: 'China',              code: 'CN' },
  { nombre: 'Uzbekistán',         code: 'UZ' },
  // CAF
  { nombre: 'Marruecos',          code: 'MA' },
  { nombre: 'Nigeria',            code: 'NG' },
  { nombre: 'Senegal',            code: 'SN' },
  { nombre: 'Costa de Marfil',    code: 'CI' },
  { nombre: 'Egipto',             code: 'EG' },
  { nombre: 'Ghana',              code: 'GH' },
  { nombre: 'Camerún',            code: 'CM' },
  { nombre: 'Sudáfrica',          code: 'ZA' },
  { nombre: 'RD Congo',           code: 'CD' },
  { nombre: 'Argelia',            code: 'DZ' },
  { nombre: 'Mali',               code: 'ML' },
  { nombre: 'Túnez',              code: 'TN' },
  { nombre: 'Angola',             code: 'AO' },
  // OFC
  { nombre: 'Nueva Zelanda',      code: 'NZ' },
];

// Sedes de respaldo si la API no devuelve ciudades
const CIUDADES_MUNDIAL_FALLBACK = [
  'New York', 'Los Angeles', 'Dallas', 'San Francisco Bay Area', 'Miami',
  'Seattle', 'Boston', 'Chicago', 'Houston', 'Atlanta',
  'Philadelphia', 'Kansas City', 'Cincinnati', 'Nashville',
  'Ciudad de México', 'Guadalajara', 'Monterrey',
  'Toronto', 'Vancouver',
];

// ===== Sub-componentes de Preferencias =====

const PreferenceSection = ({ icon, title, sub, badge, children }) => (
  <div className="pref-section">
    <div className="pref-section-header">
      <div className="pref-section-icon-wrap">{icon}</div>
      <div className="pref-section-title-group">
        <div className="pref-section-title">{title}</div>
        <div className="pref-section-sub">{sub}</div>
      </div>
      {badge > 0 && <div className="pref-section-badge">{badge}</div>}
    </div>
    {children}
  </div>
);

const PreferenceChips = ({ selected, onSelected, options, placeholder = 'Buscar…', chipClass = '' }) => {
  const [query, setQuery] = useState('');
  const [open, setOpen]   = useState(false);
  const inputRef          = useRef(null);

  const filtered = (() => {
    const pool = options.filter(o => !selected.includes(o.nombre));
    if (!query) return pool.slice(0, 8);
    return pool.filter(o => o.nombre.toLowerCase().includes(query.toLowerCase())).slice(0, 10);
  })();

  const add = (nombre) => {
    if (!selected.includes(nombre)) onSelected([...selected, nombre]);
    setQuery('');
    inputRef.current?.focus();
  };

  const remove = (nombre) => onSelected(selected.filter(s => s !== nombre));

  const handleKey = (e) => {
    if (e.key === 'Enter' && query.trim()) {
      e.preventDefault();
      if (filtered[0]) {
        add(filtered[0].nombre);
      } else {
        const t = query.trim();
        if (!selected.includes(t)) onSelected([...selected, t]);
        setQuery('');
      }
    }
    if (e.key === 'Backspace' && !query && selected.length > 0) {
      remove(selected.at(-1));
    }
  };

  return (
    <div>
      <div className="pref-chips-box" onClick={() => inputRef.current?.focus()}>
        {selected.map(s => {
          const opt = options.find(o => o.nombre === s);
          return (
            <span key={s} className={`pref-chip${chipClass ? ` ${chipClass}` : ''}`}>
              {opt?.code && (
                <ReactCountryFlag
                  countryCode={opt.code}
                  svg
                  style={{ width: 14, height: 10, borderRadius: 2, display: 'block', flexShrink: 0 }}
                />
              )}
              {s}
              <button
                className="pref-chip-x"
                onClick={(e) => { e.stopPropagation(); remove(s); }}
                aria-label={`Quitar ${s}`}
              >×</button>
            </span>
          );
        })}
        <input
          ref={inputRef}
          className="pref-chips-input"
          value={query}
          onChange={(e) => { setQuery(e.target.value); setOpen(true); }}
          onFocus={() => setOpen(true)}
          onBlur={() => setTimeout(() => setOpen(false), 200)}
          onKeyDown={handleKey}
          placeholder={selected.length === 0 ? placeholder : ''}
        />
      </div>
      {open && filtered.length > 0 && (
        <div className="pref-suggestions">
          {filtered.map(opt => (
            <button
              key={opt.nombre}
              className="pref-suggestion-item"
              onMouseDown={(e) => { e.preventDefault(); add(opt.nombre); }}
            >
              {opt.code && (
                <ReactCountryFlag
                  countryCode={opt.code}
                  svg
                  style={{ width: 16, height: 11, borderRadius: 2, display: 'block', flexShrink: 0 }}
                />
              )}
              {opt.nombre}
            </button>
          ))}
        </div>
      )}
    </div>
  );
};

const ToggleCard = ({ icon, label, desc, value, onChange }) => (
  <button
    type="button"
    className={`pref-toggle-card${value ? ' pref-toggle-card--on' : ''}`}
    onClick={() => onChange(!value)}
  >
    <span className="pref-tc-icon">{icon}</span>
    <span className="pref-tc-text">
      <span className="pref-tc-label">{label}</span>
      <span className="pref-tc-desc">{desc}</span>
    </span>
    <span className="pref-tc-pill" aria-hidden="true">
      <span className="pref-tc-knob" />
    </span>
  </button>
);

// ======= PROGRESS RING =======
const ProgressRing = ({ pct, size = 120, stroke = 10 }) => {
  const r = (size - stroke) / 2;
  const circ = 2 * Math.PI * r;
  const offset = circ - (Math.min(pct, 100) / 100) * circ;
  return (
    <svg width={size} height={size} style={{ transform: 'rotate(-90deg)', display: 'block' }}>
      <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="var(--line)" strokeWidth={stroke} />
      <circle
        cx={size / 2} cy={size / 2} r={r} fill="none"
        stroke="var(--accent)" strokeWidth={stroke}
        strokeDasharray={circ} strokeDashoffset={offset}
        strokeLinecap="round"
        style={{ transition: 'stroke-dashoffset .6s ease' }}
      />
    </svg>
  );
};

// ======= LOGROS =======
const LOGROS_DEF = [
  { id: 'bienvenido',    icon: '👋', name: 'Bienvenido al Hub',  desc: 'Cuenta creada y verificada',          check: ()        => true },
  { id: 'coleccionista', icon: '🃏', name: 'Coleccionista',      desc: 'Abriste tu primer paquete de cromos', check: (a)       => a.laminasObtenidas > 0 },
  { id: 'mitad',         icon: '📚', name: 'Medio álbum',        desc: 'Completaste el 50% del álbum',        check: (a)       => a.porcentaje >= 50 },
  { id: 'completo',      icon: '🏆', name: 'Álbum completo',     desc: 'Completaste el álbum al 100%',        check: (a)       => a.porcentaje >= 100 },
  { id: 'planificador',  icon: '📅', name: 'Planificador',       desc: 'Agendaste tu primer partido',         check: (_, ag)   => ag.length > 0 },
  { id: 'hincha',        icon: '⚽', name: 'Hincha dedicado',    desc: 'Tienes 5 o más partidos en agenda',   check: (_, ag)   => ag.length >= 5 },
];

const formatDate = (d) => {
  if (!d) return '';
  try { return new Date(d).toLocaleDateString('es', { day: 'numeric', month: 'short', year: 'numeric' }); }
  catch { return String(d); }
};

const formatTimeAgo = (d) => {
  if (!d) return '';
  try {
    const diff = Date.now() - new Date(d).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 1)  return 'Hace un momento';
    if (mins < 60) return `Hace ${mins} min`;
    const h = Math.floor(mins / 60);
    if (h < 24)   return `Hace ${h}h`;
    const days = Math.floor(h / 24);
    if (days < 7) return `Hace ${days} día${days !== 1 ? 's' : ''}`;
    return formatDate(d);
  } catch { return ''; }
};

const STATUS_MAP = {
  PROGRAMADO: { text: 'Programado', cls: 'badge-scheduled' },
  EN_JUEGO:   { text: 'En vivo',    cls: 'badge-live' },
  FINALIZADO: { text: 'Finalizado', cls: 'badge-done' },
};

const buildActivityFeed = (agenda) =>
  agenda.map((item) => {
    const p         = item.partido;
    const localName = p?.equipoLocal    || '';
    const visitName = p?.equipoVisitante || '';
    const dt        = p?.fechaHora ? new Date(p.fechaHora) : null;
    return {
      id:        item.id,
      icon:      'cal',
      iconColor: 'accent',
      label:     'Partido agendado',
      title:     localName && visitName ? `${localName} vs ${visitName}` : `Partido #${item.idPartido}`,
      subtitle:  [p?.sede?.nombreEstadio, p?.sede?.ciudad].filter(Boolean).join(' · '),
      matchDate: dt ? dt.toLocaleDateString('es', { day: 'numeric', month: 'short', year: 'numeric' }) : '',
      matchTime: dt ? dt.toLocaleTimeString('es', { hour: '2-digit', minute: '2-digit' }) : null,
      agendedAt: item.fechaAgendado,
      link:      '/calendario',
      teams: [
        localName ? { iso2: teamIso2(localName), nombre: localName } : null,
        visitName ? { iso2: teamIso2(visitName), nombre: visitName } : null,
      ].filter(Boolean),
      status:    p?.estado,
    };
  });

const ActivityCard = ({ activity }) => {
  const badge = STATUS_MAP[activity.status];
  return (
    <a href={activity.link} className="activity-card">
      <div className={`activity-card-icon activity-card-icon--${activity.iconColor}`}>
        <Icon name={activity.icon} size={17} />
      </div>
      <div className="activity-card-content">
        <div className="activity-card-top">
          <span className="activity-card-label">{activity.label}</span>
          {badge && (
            <span className={`activity-card-badge ${badge.cls}`}>
              {badge.cls === 'badge-live' && <span className="live-dot" />}
              {badge.text}
            </span>
          )}
        </div>
        <div className="activity-card-title">{activity.title}</div>
        {activity.teams.length > 0 && (
          <div className="activity-card-flags">
            {activity.teams.map((t, i) => (
              <React.Fragment key={t.nombre || i}>
                {i > 0 && <span className="activity-card-vs">vs</span>}
                <TeamFlag iso2={t.iso2} size="row" />
                <span className="activity-card-team">{t.nombre}</span>
              </React.Fragment>
            ))}
          </div>
        )}
        <div className="activity-card-meta">
          {activity.subtitle && (
            <span className="activity-card-meta-item">
              <Icon name="pin" size={11} />
              {activity.subtitle}
            </span>
          )}
          {activity.matchDate && (
            <span className="activity-card-meta-item">
              <Icon name="cal" size={11} />
              {activity.matchDate}
              {activity.matchTime && ` · ${activity.matchTime}`}
            </span>
          )}
        </div>
        {activity.agendedAt && (
          <div className="activity-card-time">
            <Icon name="clock" size={11} />
            {formatTimeAgo(activity.agendedAt)}
          </div>
        )}
      </div>
      <div className="activity-card-arrow">
        <Icon name="arrow-r" size={16} />
      </div>
    </a>
  );
};

// ======= MAIN =======
const MiCuenta = () => {
  const { dark, toggleDark } = useDarkMode();
  const { showToast } = useToast();
  const [user] = useState(() => { try { return JSON.parse(localStorage.getItem('user') || 'null'); } catch { return null; } });
  const [activeTab, setActiveTab] = useState('perfil');
  const setToast = showToast;

  // Prefs
  const [prefs, setPrefs]           = useState(null);
  const [prefsLoading, setPrefsLoading] = useState(true);
  const [selecciones, setSelecciones]   = useState([]);
  const [ciudades, setCiudades]         = useState([]);
  const [notifPush, setNotifPush]       = useState(false);
  const [notifEmail, setNotifEmail]     = useState(false);
  const [prefsSaving, setPrefsSaving]   = useState(false);
  const [sedesCiudades, setSedesCiudades] = useState([]);

  // Álbum
  const [album, setAlbum] = useState({ laminasObtenidas: 0, totalLaminas: 0, porcentaje: 0 });
  const [albumLoading, setAlbumLoading] = useState(true);

  // Historial
  const [agenda, setAgenda] = useState([]);
  const [agendaLoading, setAgendaLoading] = useState(true);

  useEffect(() => {
    if (!localStorage.getItem('token')) window.location.href = '/';
  }, []);

  const userId      = user?.usuario?.id || user?.id;
  const userName    = user?.usuario?.nombres || 'Usuario';
  const userLastName = user?.usuario?.apellidos || '';
  const userEmail   = user?.usuario?.correoUsuario || '';
  const userRole    = user?.usuario?.rol || '';
  const computedInitials = ((userName[0] || '') + (userLastName[0] || '')).toUpperCase() || '?';
  const userInitials = (user?.initials && user.initials !== '?') ? user.initials : computedInitials;

  // Cargar preferencias del usuario
  useEffect(() => {
    if (!userId) return;
    obtenerPreferencias(userId)
      .then((data) => {
        setPrefs(data);
        setSelecciones(data?.seleccionesFavoritas ? data.seleccionesFavoritas.split(',').map(s => s.trim()).filter(Boolean) : []);
        setCiudades(data?.ciudadesInteres ? data.ciudadesInteres.split(',').map(s => s.trim()).filter(Boolean) : []);
        setNotifPush(!!data?.notifPush);
        setNotifEmail(!!data?.notifEmail);
      })
      .catch(() => {})
      .finally(() => setPrefsLoading(false));
  }, [userId]);

  // Cargar ciudades sedes desde la API
  useEffect(() => {
    getAllSedes()
      .then(res => {
        const data = Array.isArray(res?.data) ? res.data : [];
        const cities = [...new Set(data.map(s => s.ciudad || s.nombre).filter(Boolean))].sort();
        if (cities.length > 0) setSedesCiudades(cities);
      })
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (!userId) return;
    getAlbumUsuario(userId)
      .then((res) => {
        const d = res?.data ?? res;
        if (d && typeof d === 'object' && 'laminasObtenidas' in d) {
          setAlbum({
            laminasObtenidas: d.laminasObtenidas ?? 0,
            totalLaminas: d.totalLaminas ?? 0,
            porcentaje: d.porcentaje ?? 0,
          });
        }
      })
      .catch(() => {})
      .finally(() => setAlbumLoading(false));
  }, [userId]);

  useEffect(() => {
    if (!userId) return;
    getAgendaUsuario(userId)
      .then(async (res) => {
        const items = Array.isArray(res?.data) ? res.data : [];
        const enriched = await Promise.all(
          items.map(async (item) => {
            if (item.partido) return item;
            try {
              const { data } = await getPartidoPorId(item.idPartido);
              return { ...item, partido: data };
            } catch {
              return item;
            }
          })
        );
        setAgenda(enriched);
      })
      .catch(() => setAgenda([]))
      .finally(() => setAgendaLoading(false));
  }, [userId]);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/';
  };

  const handleSavePrefs = async () => {
    if (!userId) return;
    setPrefsSaving(true);
    const datos = {
      seleccionesFavoritas: selecciones.join(', '),
      ciudadesInteres: ciudades.join(', '),
      notifPush: notifPush ? 1 : 0,
      notifEmail: notifEmail ? 1 : 0,
    };
    try {
      if (prefs) {
        await editarPreferencias(userId, datos);
      } else {
        await guardarPreferencias(userId, datos);
      }
      setPrefs({ ...prefs, ...datos });
      setToast('Preferencias guardadas correctamente');
    } catch {
      try {
        await guardarPreferencias(userId, datos);
        setPrefs({ ...prefs, ...datos });
        setToast('Preferencias guardadas correctamente');
      } catch {
        setToast('Error al guardar preferencias. Intenta de nuevo.');
      }
    } finally {
      setPrefsSaving(false);
    }
  };

  const logros = LOGROS_DEF.map((l) => ({ ...l, unlocked: l.check(album, agenda) }));

  const TABS = [
    { id: 'perfil',       label: 'Perfil',       icon: 'user' },
    { id: 'preferencias', label: 'Preferencias', icon: 'settings' },
    { id: 'album',        label: 'Álbum',         icon: 'cards' },
    { id: 'historial',    label: 'Historial',     icon: 'list' },
  ];

  // Opciones de ciudades: primero API, luego fallback
  const ciudadOptions = (sedesCiudades.length > 0 ? sedesCiudades : CIUDADES_MUNDIAL_FALLBACK)
    .map(c => ({ nombre: c }));

  // Resumen para el botón de guardar
  const saveSummary = [
    selecciones.length > 0 && `${selecciones.length} selección${selecciones.length !== 1 ? 'es' : ''}`,
    ciudades.length > 0    && `${ciudades.length} ciudad${ciudades.length !== 1 ? 'es' : ''}`,
  ].filter(Boolean).join(' · ');

  return (
    <div className="app">
      <Navbar
        dark={dark} onToggleDark={toggleDark}
        loggedIn={true} user={user}
        onLogin={() => {}} onRegister={() => {}} onLogout={handleLogout}
      />

      {/* ── Hero perfil ── */}
      <div className="mcuenta-hero">
        <div className="container">
          <div className="mcuenta-hero-inner">
            <div className="mcuenta-avatar">{userInitials}</div>
            <div className="mcuenta-hero-info">
              <div className="mcuenta-name">{userName}{userLastName ? ` ${userLastName}` : ''}</div>
              <div className="mcuenta-email">{userEmail}</div>
              <div className="mcuenta-hero-meta">
                {userRole && (
                  <div className="mcuenta-role">
                    <Icon name="shield" size={11} />
                    {userRole.charAt(0).toUpperCase() + userRole.slice(1).toLowerCase()}
                  </div>
                )}
                {userId && (
                  <div className="mcuenta-uid">#{userId}</div>
                )}
              </div>
            </div>
          </div>
          <div className="mcuenta-tabs">
            {TABS.map((t) => (
              <button
                key={t.id}
                className={`mcuenta-tab${activeTab === t.id ? ' active' : ''}`}
                onClick={() => setActiveTab(t.id)}
              >
                {t.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* ── Contenido de tabs ── */}
      <div className="mcuenta-body">
        <div className="container">

          {/* ── PERFIL ── */}
          {activeTab === 'perfil' && (
            <>
              <div className="mcuenta-section">
                <div className="mcuenta-section-title">
                  <Icon name="user" size={13} /> Información personal
                </div>
                <div className="mcuenta-info-grid">
                  <div className="mcuenta-info-card">
                    <div className="mcuenta-info-label">Nombre</div>
                    <div className="mcuenta-info-value">{userName ? userName.toUpperCase() : '—'}</div>
                  </div>
                  <div className="mcuenta-info-card">
                    <div className="mcuenta-info-label">Apellidos</div>
                    <div className="mcuenta-info-value">{userLastName ? userLastName.toUpperCase() : '—'}</div>
                  </div>
                  <div className="mcuenta-info-card">
                    <div className="mcuenta-info-label">Correo electrónico</div>
                    <div className="mcuenta-info-value" style={{ fontSize: 13, wordBreak: 'break-all' }}>{userEmail || '—'}</div>
                  </div>
                  <div className="mcuenta-info-card">
                    <div className="mcuenta-info-label">Rol</div>
                    <div className="mcuenta-info-value">
                      {userRole ? userRole.charAt(0).toUpperCase() + userRole.slice(1).toLowerCase() : '—'}
                    </div>
                  </div>
                  <div className="mcuenta-info-card">
                    <div className="mcuenta-info-label">Partidos en agenda</div>
                    <div className="mcuenta-info-value">{agendaLoading ? '…' : agenda.length}</div>
                  </div>
                  <div className="mcuenta-info-card">
                    <div className="mcuenta-info-label">Álbum completado</div>
                    <div className="mcuenta-info-value">
                      {albumLoading ? '…' : `${Math.round(album.porcentaje)}%`}
                    </div>
                  </div>
                </div>
              </div>

              <div className="mcuenta-section">
                <div className="mcuenta-section-title">
                  <Icon name="trophy" size={13} /> Logros
                </div>
                <div className="logros-grid">
                  {logros.map((l) => (
                    <div key={l.id} className={`logro-badge${l.unlocked ? ' unlocked' : ' locked'}`}>
                      <div className="logro-icon">{l.icon}</div>
                      <div>
                        <div className="logro-name">{l.name}</div>
                        <div className="logro-desc">{l.desc}</div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </>
          )}

          {/* ── PREFERENCIAS ── */}
          {activeTab === 'preferencias' && (
            <>
              {prefsLoading ? (
                <div className="pref-loading">
                  <div className="pref-loading-spinner" />
                  <span>Cargando preferencias…</span>
                </div>
              ) : (
                <div className="pref-container">

                  <PreferenceSection
                    icon={<Icon name="soccer" size={20} />}
                    title="Selecciones favoritas"
                    sub="Elige los equipos que quieres seguir en el Mundial 2026"
                    badge={selecciones.length}
                  >
                    <PreferenceChips
                      selected={selecciones}
                      onSelected={setSelecciones}
                      options={EQUIPOS_MUNDIAL}
                      placeholder="Buscar equipo o selección…"
                    />
                  </PreferenceSection>

                  <PreferenceSection
                    icon={<Icon name="pin" size={20} />}
                    title="Ciudades de interés"
                    sub="Sedes del Mundial 2026 que quieres seguir de cerca"
                    badge={ciudades.length}
                  >
                    <PreferenceChips
                      selected={ciudades}
                      onSelected={setCiudades}
                      options={ciudadOptions}
                      placeholder="Buscar ciudad sede…"
                      chipClass="pref-chip--city"
                    />
                  </PreferenceSection>

                  <PreferenceSection
                    icon={<Icon name="bell" size={20} />}
                    title="Notificaciones"
                    sub="Elige cómo recibir alertas de tus equipos y partidos favoritos"
                  >
                    <div className="pref-toggle-cards">
                      <ToggleCard
                        icon={<Icon name="bell" size={18} />}
                        label="Notificaciones push"
                        desc="Alertas en tiempo real en el navegador"
                        value={notifPush}
                        onChange={setNotifPush}
                      />
                      <ToggleCard
                        icon={<Icon name="mail" size={18} />}
                        label="Notificaciones por correo"
                        desc="Resumen semanal de tus partidos y alertas especiales"
                        value={notifEmail}
                        onChange={setNotifEmail}
                      />
                    </div>
                  </PreferenceSection>

                  <div className="pref-save-cta">
                    <div className="pref-save-summary">{saveSummary}</div>
                    <button
                      className="pref-save-btn btn btn-primary"
                      onClick={handleSavePrefs}
                      disabled={prefsSaving}
                    >
                      {prefsSaving ? (
                        <><span className="pref-save-spinner" />Guardando…</>
                      ) : (
                        <><Icon name="check" size={14} />Guardar preferencias</>
                      )}
                    </button>
                  </div>

                </div>
              )}
            </>
          )}

          {/* ── ÁLBUM ── */}
          {activeTab === 'album' && (
            <>
              {albumLoading ? (
                <div style={{ color: 'var(--ink-3)', padding: '24px 0' }}>Cargando álbum…</div>
              ) : (
                <>
                  <div className="album-hero">
                    <div className="album-ring-wrap">
                      <ProgressRing pct={album.porcentaje} size={120} stroke={10} />
                      <div className="album-ring-center">
                        <div className="album-ring-pct">{Math.round(album.porcentaje)}%</div>
                        <div className="album-ring-label">completado</div>
                      </div>
                    </div>
                    <div className="album-stats">
                      <div className="album-stat">
                        <div className="album-stat-num">{album.laminasObtenidas}</div>
                        <div className="album-stat-label">Láminas obtenidas</div>
                      </div>
                      <div className="album-stat">
                        <div className="album-stat-num" style={{ color: 'var(--ink-3)' }}>{album.totalLaminas}</div>
                        <div className="album-stat-label">Total en el álbum</div>
                      </div>
                      <div className="album-stat">
                        <div className="album-stat-num" style={{ color: 'var(--accent)' }}>
                          {Math.max(0, album.totalLaminas - album.laminasObtenidas)}
                        </div>
                        <div className="album-stat-label">Faltan conseguir</div>
                      </div>
                    </div>
                  </div>

                  {album.laminasObtenidas === 0 ? (
                    <div style={{ textAlign: 'center', color: 'var(--ink-3)', fontSize: 14, padding: '24px 0' }}>
                      <div style={{ fontSize: 40, marginBottom: 10 }}>🃏</div>
                      <div style={{ fontWeight: 600, color: 'var(--ink)', marginBottom: 6 }}>Tu álbum está vacío</div>
                      Aún no has abierto ningún paquete de cromos. ¡Empieza tu colección desde el inicio!
                    </div>
                  ) : (
                    <div style={{ marginTop: 8, padding: '14px 18px', background: 'var(--bg-elev)', borderRadius: 12, border: '1px solid var(--line)', fontSize: 13, color: 'var(--ink-3)' }}>
                      Llevas <strong style={{ color: 'var(--ink)' }}>{album.laminasObtenidas}</strong> cromos de{' '}
                      <strong style={{ color: 'var(--ink)' }}>{album.totalLaminas}</strong> posibles.
                      {album.porcentaje >= 50
                        ? ' ¡Vas por buen camino, sigue abriendo paquetes!'
                        : ' ¡Abre más paquetes para completar tu colección!'}
                    </div>
                  )}
                </>
              )}
            </>
          )}

          {/* ── HISTORIAL ── */}
          {activeTab === 'historial' && (() => {
            if (agendaLoading) return (
              <div className="activity-loading">
                <div className="pref-loading-spinner" />
                <span>Cargando actividad…</span>
              </div>
            );
            const feed = buildActivityFeed(agenda);
            if (feed.length === 0) return (
              <div className="activity-empty">
                <div className="activity-empty-icon">
                  <Icon name="zap" size={30} />
                </div>
                <div className="activity-empty-title">Sin actividad reciente</div>
                <div className="activity-empty-desc">
                  Aún no tienes actividad registrada. Explora el calendario, abre tu álbum o únete a una polla para empezar.
                </div>
                <div className="activity-empty-ctas">
                  <a href="/calendario" className="btn btn-primary">
                    <Icon name="cal" size={14} />
                    Explorar partidos
                  </a>
                  <a href="/album" className="btn btn-ghost">
                    <Icon name="cards" size={14} />
                    Abrir álbum
                  </a>
                  <a href="/pollas" className="btn btn-ghost">
                    <Icon name="trophy" size={14} />
                    Ver pollas
                  </a>
                </div>
              </div>
            );
            return (
              <div className="activity-feed">
                <div className="activity-feed-header">
                  <div className="activity-feed-title">
                    <Icon name="zap" size={15} />
                    Actividad reciente
                  </div>
                  <div className="activity-feed-count">
                    {feed.length} evento{feed.length !== 1 ? 's' : ''}
                  </div>
                </div>
                <div className="activity-timeline">
                  {feed.map((activity, idx) => (
                    <div className="activity-timeline-item" key={activity.id || idx}>
                      <div className="activity-timeline-dot" />
                      <ActivityCard activity={activity} />
                    </div>
                  ))}
                </div>
              </div>
            );
          })()}

        </div>
      </div>

      <Footer />
    </div>
  );
};

export default MiCuenta;
