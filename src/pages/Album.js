import React, { useState, useEffect, useCallback } from 'react';
import Navbar from '../components/layout/Navbar';
import Footer from '../components/layout/Footer';
import Icon from '../components/ui/Icon';
import LoginModal from '../components/auth/LoginModal';
import RegisterModal from '../components/auth/RegisterModal';
import PageHero from '../components/common/PageHero';
import useDarkMode from '../hooks/useDarkMode';
import useAuth from '../hooks/useAuth';
import {
  getAlbumUsuario,
  getAllLaminas,
  getLaminasRepetidas,
  abrirSobre,
  getIntercambios,
  aceptarIntercambio,
  rechazarIntercambio,
  solicitarIntercambio,
} from '../services/albumService';

// ─── Constants ────────────────────────────────────────────────────────────────
const RAREZAS = {
  COMUN:      { label: 'Común',       color: '#9094bc', glow: 'rgba(144,148,188,.4)'  },
  RARA:       { label: 'Rara',        color: '#304ffe', glow: 'rgba(48,79,254,.45)'   },
  EPICA:      { label: 'Épica',       color: '#8b5cf6', glow: 'rgba(139,92,246,.45)'  },
  LEGENDARIA: { label: 'Legendaria',  color: '#ff8a00', glow: 'rgba(255,138,0,.5)'    },
};

const TABS = [
  { id: 'coleccion',    label: 'Mi Colección',  icon: 'grid'    },
  { id: 'sobres',       label: 'Abrir Sobres',  icon: 'cards'   },
  { id: 'repetidas',    label: 'Repetidas',     icon: 'refresh' },
  { id: 'intercambios', label: 'Intercambios',  icon: 'users'   },
  { id: 'progreso',     label: 'Progreso',      icon: 'trophy'  },
];

const COL_FILTER = [
  { id: 'todas',      label: 'Todas'        },
  { id: 'obtenidas',  label: 'Obtenidas'    },
  { id: 'pendientes', label: 'Por obtener'  },
];

// ─── Helpers ──────────────────────────────────────────────────────────────────
const normalizeRareza = (r) => {
  if (!r) return 'COMUN';
  const upper = String(r).toUpperCase()
    .normalize('NFD').replace(/[̀-ͯ]/g, '');
  if (RAREZAS[upper]) return upper;
  return 'COMUN';
};
const getRareza = (r) => RAREZAS[normalizeRareza(r)];

// ─── Empty State ──────────────────────────────────────────────────────────────
const EmptyState = ({ icon = 'cards', title, sub, action }) => (
  <div className="alb-empty">
    <div className="alb-empty-icon"><Icon name={icon} size={44} /></div>
    <p className="alb-empty-title">{title}</p>
    <p className="alb-empty-sub">{sub}</p>
    {action && <div className="alb-empty-action">{action}</div>}
  </div>
);

// ─── Auth Required ────────────────────────────────────────────────────────────
const AuthRequired = ({ onLogin }) => (
  <div className="alb-auth-required">
    <div className="alb-auth-icon"><Icon name="shield" size={40} /></div>
    <p className="alb-auth-title">Acceso restringido</p>
    <p className="alb-auth-sub">Inicia sesión para gestionar tu álbum del Mundial.</p>
    <button className="btn btn-primary btn-lg" onClick={onLogin}>Iniciar sesión</button>
  </div>
);

// ─── Progress Ring ────────────────────────────────────────────────────────────
const ProgressRing = ({ pct, size = 160, stroke = 12, color = 'var(--accent)' }) => {
  const r = (size - stroke) / 2;
  const circ = 2 * Math.PI * r;
  const offset = circ - (Math.min(pct, 100) / 100) * circ;
  return (
    <div className="alb-ring-wrap" style={{ width: size, height: size }}>
      <svg width={size} height={size} style={{ transform: 'rotate(-90deg)' }}>
        <circle cx={size/2} cy={size/2} r={r} fill="none" stroke="var(--bg-soft-2)" strokeWidth={stroke} />
        <circle
          cx={size/2} cy={size/2} r={r} fill="none"
          stroke={color} strokeWidth={stroke} strokeLinecap="round"
          strokeDasharray={circ} strokeDashoffset={offset}
          style={{ transition: 'stroke-dashoffset 1.2s cubic-bezier(.2,.8,.2,1)' }}
        />
      </svg>
      <div className="alb-ring-center">
        <span className="alb-ring-pct">{pct}%</span>
        <span className="alb-ring-lbl">completado</span>
      </div>
    </div>
  );
};

// ─── Sticker Slot ─────────────────────────────────────────────────────────────
const StickerSlot = ({ lamina, owned }) => {
  const num = String(lamina.numeroAlbum || lamina.id || '?').padStart(3, '0');
  const rareza = getRareza(lamina.rareza);

  if (!owned) {
    return (
      <div className="alb-slot alb-slot-empty" title={`Lámina #${num} — no obtenida`}>
        <div className="alb-slot-num">{num}</div>
        <div className="alb-slot-silhouette" />
        <div className="alb-slot-lock"><Icon name="shield" size={11} /></div>
      </div>
    );
  }

  return (
    <div
      className="alb-slot alb-slot-filled"
      style={{ '--slot-color': rareza.color, '--slot-glow': rareza.glow }}
      title={`${lamina.nombre} · ${lamina.seleccion}`}
    >
      <div className="alb-slot-rareza-bar" />
      <div className="alb-slot-num" style={{ color: rareza.color }}>{num}</div>
      {lamina.imagenUrl
        ? <img src={lamina.imagenUrl} alt={lamina.nombre} className="alb-slot-img" />
        : <div className="alb-slot-initial">{(lamina.nombre || '?')[0].toUpperCase()}</div>
      }
      <div className="alb-slot-name">{lamina.nombre}</div>
      <div className="alb-slot-seleccion">{lamina.seleccion || ''}</div>
      <span className="alb-slot-rareza-badge">{rareza.label}</span>
    </div>
  );
};

// ─── Pack Reveal Overlay ──────────────────────────────────────────────────────
const PackReveal = ({ laminas, prevIds, onClose }) => {
  const [flipped, setFlipped] = useState([]);
  const [allDone, setAllDone] = useState(false);

  useEffect(() => {
    let i = 0;
    const tick = () => {
      if (i < laminas.length) {
        const idx = i;
        setFlipped(prev => [...prev, idx]);
        i++;
        setTimeout(tick, 500);
      } else {
        setTimeout(() => setAllDone(true), 300);
      }
    };
    const start = setTimeout(tick, 600);
    return () => clearTimeout(start);
  }, [laminas.length]);

  const newCount  = laminas.filter(l => !prevIds.has(l.id)).length;
  const repCount  = laminas.length - newCount;

  return (
    <div className="alb-reveal-overlay" onClick={(e) => e.target === e.currentTarget && allDone && onClose()}>
      <div className="alb-reveal-modal">
        <div className="alb-reveal-header">
          <span className="alb-reveal-eyebrow">
            <Icon name="sparkles" size={13} /> Sobre abierto
          </span>
          <h2 className="alb-reveal-title">¡Obtuviste 5 láminas!</h2>
          {allDone && (
            <div className="alb-reveal-summary">
              <span className="alb-reveal-chip new">{newCount} nueva{newCount !== 1 ? 's' : ''}</span>
              {repCount > 0 && (
                <span className="alb-reveal-chip rep">{repCount} repetida{repCount !== 1 ? 's' : ''}</span>
              )}
            </div>
          )}
        </div>

        <div className="alb-reveal-cards">
          {laminas.map((l, i) => {
            const rareza = getRareza(l.rareza);
            const isNew = !prevIds.has(l.id);
            const isFlipped = flipped.includes(i);
            const isLegendaria = l.rareza === 'LEGENDARIA';
            const isEpica = l.rareza === 'EPICA';

            return (
              <div key={i} className={`alb-reveal-card-wrap${isFlipped ? ' flipped' : ''}`}>
                <div className="alb-reveal-card-inner">
                  {/* Back (face-down) */}
                  <div className="alb-reveal-card-back">
                    <div className="alb-reveal-back-logo">26</div>
                    <div className="alb-reveal-back-stars">
                      {[...Array(3)].map((_, s) => <Icon key={s} name="star-fill" size={10} />)}
                    </div>
                  </div>
                  {/* Front (face-up) */}
                  <div
                    className={`alb-reveal-card-front${isLegendaria ? ' legendary' : isEpica ? ' epic' : ''}`}
                    style={{ '--card-color': rareza.color, '--card-glow': rareza.glow }}
                  >
                    <div className="alb-reveal-card-top-bar" />
                    <div className="alb-reveal-card-num">
                      {String(l.numeroAlbum || l.id || '?').padStart(3, '0')}
                    </div>
                    {l.imagenUrl
                      ? <img src={l.imagenUrl} alt={l.nombre} className="alb-reveal-card-img" />
                      : (
                        <div className="alb-reveal-card-initial">
                          {(l.nombre || '?')[0].toUpperCase()}
                        </div>
                      )
                    }
                    <div className="alb-reveal-card-name">{l.nombre || 'Jugador'}</div>
                    <div className="alb-reveal-card-sel">{l.seleccion || ''}</div>
                    <div className="alb-reveal-card-rareza" style={{ color: rareza.color }}>
                      {rareza.label}
                    </div>
                    {isNew && <span className="alb-reveal-card-new-badge">NUEVA</span>}
                    {isLegendaria && <div className="alb-reveal-card-shine" />}
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        {allDone && (
          <button className="alb-reveal-close-btn" onClick={onClose}>
            <Icon name="check" size={16} /> ¡A coleccionar!
          </button>
        )}
      </div>
    </div>
  );
};

// ─── Pack Card ────────────────────────────────────────────────────────────────
const PackCard = ({ onOpen, opening, loggedIn, onLogin }) => (
  <div className="alb-pack-section">
    <div className="alb-pack-visual">
      <div
        className={`alb-pack-card${opening ? ' alb-pack-opening' : ''}`}
        onClick={loggedIn && !opening ? onOpen : undefined}
        role="button" tabIndex={0}
      >
        <div className="alb-pack-shine" />
        <div className="alb-pack-logo">26</div>
        <div className="alb-pack-stars">
          {[...Array(5)].map((_, i) => <Icon key={i} name="star-fill" size={11} />)}
        </div>
        <div className="alb-pack-label">MUNDIAL HUB</div>
      </div>
    </div>

    <div className="alb-pack-info">
      <span className="alb-pack-eyebrow">Colección Oficial</span>
      <h3 className="alb-pack-title">Sobre Mundialista</h3>
      <p className="alb-pack-sub">
        Cada sobre contiene <strong>5 láminas aleatorias</strong> del Mundial 2026.
        Puedes encontrar láminas raras, épicas y legendarias.
      </p>

      {loggedIn ? (
        <button className="alb-pack-btn" onClick={onOpen} disabled={opening}>
          {opening
            ? <><div className="spinner-sm" /> Abriendo sobre…</>
            : <><Icon name="sparkles" size={16} /> Abrir Sobre</>
          }
        </button>
      ) : (
        <button className="alb-pack-btn" onClick={onLogin}>
          <Icon name="shield" size={16} /> Iniciar sesión para abrir sobres
        </button>
      )}

      <p className="alb-pack-note">
        Los sobres estarán disponibles cuando se carguen los jugadores al sistema.
      </p>

      <div className="alb-pack-rarezas">
        {Object.entries(RAREZAS).map(([key, val]) => (
          <div key={key} className="alb-pack-rareza-chip" style={{ '--chip-color': val.color }}>
            <span className="alb-pack-rareza-dot" />
            {val.label}
          </div>
        ))}
      </div>
    </div>
  </div>
);

// ─── Repetida Row ─────────────────────────────────────────────────────────────
const RepetidaRow = ({ laminaUsuario, lamina }) => {
  const rareza = getRareza(lamina?.rareza);
  return (
    <div className="alb-rep-row">
      <div className="alb-rep-num">
        {String(lamina?.numeroAlbum || laminaUsuario.idLamina || '?').padStart(3, '0')}
      </div>
      <div className="alb-rep-info">
        <span className="alb-rep-name">{lamina?.nombre || `Lámina #${laminaUsuario.idLamina}`}</span>
        <span className="alb-rep-sel">{lamina?.seleccion || '—'}</span>
      </div>
      <span className="alb-rep-badge" style={{ color: rareza.color }}>{rareza.label}</span>
      <span className="alb-rep-count">×{laminaUsuario.cantidad}</span>
    </div>
  );
};

// ─── Intercambio Row ──────────────────────────────────────────────────────────
const IntercambioRow = ({ intercambio, onAceptar, onRechazar }) => {
  const estado = (intercambio.estado || 'PENDIENTE').toLowerCase();
  return (
    <div className="alb-int-row">
      <div className={`alb-int-estado alb-int-estado--${estado}`}>
        {intercambio.estado || 'PENDIENTE'}
      </div>
      <div className="alb-int-info">
        <span className="alb-int-card-title">
          Lámina #{intercambio.idLaminaOferta} → Lámina #{intercambio.idLaminaReceptor}
        </span>
        <span className="alb-int-user">
          De usuario #{intercambio.idUsuarioOferta}
        </span>
      </div>
      {estado === 'pendiente' && (
        <div style={{ display: 'flex', gap: 8, flexShrink: 0 }}>
          <button
            className="alb-int-action-btn accept"
            onClick={() => onAceptar(intercambio.id)}
            title="Aceptar"
          >
            <Icon name="check" size={14} />
          </button>
          <button
            className="alb-int-action-btn reject"
            onClick={() => onRechazar(intercambio.id)}
            title="Rechazar"
          >
            <Icon name="close" size={14} />
          </button>
        </div>
      )}
    </div>
  );
};

// ─── Progress Bar ─────────────────────────────────────────────────────────────
const ProgressBar = ({ label, value, total, color = 'var(--accent)' }) => {
  const pct = total > 0 ? Math.min(Math.round((value / total) * 100), 100) : 0;
  return (
    <div className="alb-prog-bar-row">
      <div className="alb-prog-bar-head">
        <span className="alb-prog-bar-label">{label}</span>
        <span className="alb-prog-bar-val">{value}/{total}</span>
      </div>
      <div className="alb-prog-bar-track">
        <div className="alb-prog-bar-fill" style={{ width: `${pct}%`, background: color }} />
      </div>
    </div>
  );
};

// ─── Skeleton Grid ────────────────────────────────────────────────────────────
const SkeletonGrid = () => (
  <div className="alb-grid">
    {[...Array(20)].map((_, i) => (
      <div key={i} className="alb-slot alb-slot-skel skel-pulse" />
    ))}
  </div>
);

// ─── Nueva Oferta Modal ───────────────────────────────────────────────────────
const NuevaOfertaModal = ({ repetidas, laminaMap, onClose, onSubmit, loading }) => {
  const [idLaminaOferta, setIdLaminaOferta] = useState('');
  const [idUsuarioReceptor, setIdUsuarioReceptor] = useState('');
  const [idLaminaReceptor, setIdLaminaReceptor] = useState('');
  const [error, setError] = useState('');

  const submit = (e) => {
    e.preventDefault();
    setError('');
    const off = Number(idLaminaOferta);
    const rec = Number(idUsuarioReceptor);
    const recL = Number(idLaminaReceptor);
    if (!off || !rec || !recL) { setError('Completa todos los campos.'); return; }
    onSubmit({ idLaminaOferta: off, idUsuarioReceptor: rec, idLaminaReceptor: recL });
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 480 }}>
        <button className="modal-close" onClick={onClose}><Icon name="close" size={16} /></button>
        <h2>Nueva oferta de intercambio</h2>
        <p className="modal-desc">Ofrece una de tus repetidas a otro coleccionista.</p>
        {error && <div className="modal-error">{error}</div>}
        <form onSubmit={submit}>
          <div className="input-group">
            <label className="input-label">Lámina que ofreces (de tus repetidas)</label>
            <select
              className="input"
              value={idLaminaOferta}
              onChange={(e) => setIdLaminaOferta(e.target.value)}
              required
            >
              <option value="">— Selecciona una lámina —</option>
              {repetidas.map((lu) => {
                const lam = laminaMap.get(lu.idLamina);
                const num = String(lam?.numeroAlbum || lu.idLamina).padStart(3, '0');
                const nombre = lam?.nombre || `Lámina #${lu.idLamina}`;
                return (
                  <option key={lu.id} value={lu.idLamina}>
                    #{num} — {nombre} (×{lu.cantidad})
                  </option>
                );
              })}
            </select>
          </div>
          <div className="input-group">
            <label className="input-label">ID del usuario receptor</label>
            <input
              className="input"
              type="number"
              min="1"
              placeholder="Ej: 12"
              value={idUsuarioReceptor}
              onChange={(e) => setIdUsuarioReceptor(e.target.value)}
              required
            />
          </div>
          <div className="input-group">
            <label className="input-label">ID de la lámina que pides a cambio</label>
            <input
              className="input"
              type="number"
              min="1"
              placeholder="Ej: 47"
              value={idLaminaReceptor}
              onChange={(e) => setIdLaminaReceptor(e.target.value)}
              required
            />
          </div>
          <button className="modal-btn" type="submit" disabled={loading}>
            {loading ? 'Enviando…' : 'Enviar oferta'}
          </button>
        </form>
      </div>
    </div>
  );
};

// ─── Main Component ───────────────────────────────────────────────────────────
const Album = () => {
  const { dark, setDark } = useDarkMode();
  const {
    loggedIn, user, modal,
    showToast, openLogin, openRegister, closeModal,
    handleLoginSuccess, handleRegisterSuccess, handleLogout,
  } = useAuth();

  const userId = user?.usuario?.id || user?.id;

  const [tab, setTab]                   = useState('coleccion');
  const [albumStats, setAlbumStats]     = useState({ laminasObtenidas: 0, totalLaminas: 0, porcentaje: 0 });
  const [allLaminas, setAllLaminas]     = useState([]);   // catálogo completo
  const [myLaminaIds, setMyLaminaIds]   = useState(new Set());
  const [repetidas, setRepetidas]       = useState([]);   // LaminaUsuario[]
  const [intercambios, setIntercambios] = useState([]);
  const [loading, setLoading]           = useState(false);
  const [opening, setOpening]           = useState(false);
  const [revealCards, setRevealCards]   = useState(null); // null | { laminas, prevIds }
  const [search, setSearch]             = useState('');
  const [colFilter, setColFilter]       = useState('todas');
  const [nuevaOfertaOpen, setNuevaOfertaOpen] = useState(false);
  const [enviandoOferta, setEnviandoOferta]   = useState(false);

  const loadAlbum = useCallback(async () => {
    setLoading(true);
    try {
      // Catálogo siempre disponible (sin login)
      const catRes = await getAllLaminas().catch(() => ({ data: [] }));
      const catalog = Array.isArray(catRes.data) ? catRes.data : [];
      setAllLaminas(catalog);

      if (!loggedIn || !userId) { setLoading(false); return; }

      const [albumRes, repRes, intRes] = await Promise.allSettled([
        getAlbumUsuario(userId),
        getLaminasRepetidas(userId),
        getIntercambios(userId),
      ]);

      if (albumRes.status === 'fulfilled') {
        const d = albumRes.value?.data || {};
        setAlbumStats({
          laminasObtenidas: d.laminasObtenidas || 0,
          totalLaminas:     d.totalLaminas     || catalog.length,
          porcentaje:       d.porcentaje       || 0,
        });
        const ids = new Set((d.album || []).map(lu => lu.idLamina));
        setMyLaminaIds(ids);
      }

      if (repRes.status === 'fulfilled') {
        setRepetidas(Array.isArray(repRes.value?.data) ? repRes.value.data : []);
      }

      if (intRes.status === 'fulfilled') {
        setIntercambios(Array.isArray(intRes.value?.data) ? intRes.value.data : []);
      }
    } finally {
      setLoading(false);
    }
  }, [loggedIn, userId]);

  useEffect(() => { loadAlbum(); }, [loadAlbum]);

  const handleAbrirSobre = async () => {
    if (!loggedIn || !userId) return;
    setOpening(true);
    try {
      const res = await abrirSobre(userId);
      const laminasObtenidas = res?.data?.laminasObtenidas;

      if (Array.isArray(laminasObtenidas) && laminasObtenidas.length > 0) {
        // Guardar qué IDs tenía ANTES de abrir para saber cuáles son nuevas
        const prevIds = new Set(myLaminaIds);
        await loadAlbum();
        setRevealCards({ laminas: laminasObtenidas, prevIds });
      } else {
        showToast(res?.data?.mensaje || 'No hay láminas disponibles aún.');
      }
    } catch (e) {
      const msg = e?.response?.data?.mensaje || 'No hay láminas disponibles aún. ¡Vuelve pronto!';
      showToast(msg);
    } finally {
      setOpening(false);
    }
  };

  const handleAceptarIntercambio = async (id) => {
    try {
      await aceptarIntercambio(id);
      showToast('¡Intercambio aceptado!');
      await loadAlbum();
    } catch {
      showToast('Error al aceptar el intercambio.');
    }
  };

  const handleRechazarIntercambio = async (id) => {
    try {
      await rechazarIntercambio(id);
      showToast('Intercambio rechazado.');
      setIntercambios(prev => prev.filter(i => i.id !== id));
    } catch {
      showToast('Error al rechazar el intercambio.');
    }
  };

  const handleSolicitarIntercambio = async ({ idLaminaOferta, idUsuarioReceptor, idLaminaReceptor }) => {
    if (!userId) return;
    setEnviandoOferta(true);
    try {
      await solicitarIntercambio({
        idUsuarioOferta:    userId,
        idLaminaOferta,
        idUsuarioReceptor,
        idLaminaReceptor,
      });
      showToast('¡Oferta de intercambio enviada!');
      setNuevaOfertaOpen(false);
      await loadAlbum();
    } catch (e) {
      showToast(e?.response?.data?.mensaje || 'No se pudo enviar la oferta.');
    } finally {
      setEnviandoOferta(false);
    }
  };

  // Collection filtering
  const filteredLaminas = allLaminas.filter(l => {
    const q = search.toLowerCase();
    const matchQ = !q
      || (l.nombre || '').toLowerCase().includes(q)
      || (l.seleccion || '').toLowerCase().includes(q);
    const owned = myLaminaIds.has(l.id);
    const matchFilter =
      colFilter === 'todas'      ? true  :
      colFilter === 'obtenidas'  ? owned :
      !owned;
    return matchQ && matchFilter;
  });

  const pct = albumStats.porcentaje
    || (albumStats.totalLaminas > 0
      ? Math.round((albumStats.laminasObtenidas / albumStats.totalLaminas) * 100)
      : 0);

  // Enrich repetidas with lamina catalog data
  const laminaMap = new Map(allLaminas.map(l => [l.id, l]));

  // Stats by rareza for progress tab (normalizando valores de la BD)
  const rarezaCounts = {};
  Object.keys(RAREZAS).forEach(r => {
    rarezaCounts[r] = { total: 0, owned: 0 };
  });
  allLaminas.forEach(l => {
    const r = normalizeRareza(l.rareza);
    rarezaCounts[r].total++;
    if (myLaminaIds.has(l.id)) rarezaCounts[r].owned++;
  });

  // Stats by categoria (posición) for progress tab
  const categoriaCounts = {};
  allLaminas.forEach(l => {
    const cat = (l.categoria || 'Sin categoría').trim();
    if (!categoriaCounts[cat]) categoriaCounts[cat] = { total: 0, owned: 0 };
    categoriaCounts[cat].total++;
    if (myLaminaIds.has(l.id)) categoriaCounts[cat].owned++;
  });
  const CATEGORIA_COLOR = {
    'Portero':    '#16a085',
    'Arquero':    '#16a085',
    'Defensa':    '#2980b9',
    'Defensor':   '#2980b9',
    'Mediocampista': '#8e44ad',
    'Mediocampo':    '#8e44ad',
    'Delantero':  '#e67e22',
  };

  return (
    <div className="app">
      <Navbar
        dark={dark} onToggleDark={() => setDark(!dark)}
        loggedIn={loggedIn} user={user}
        onLogin={openLogin} onRegister={openRegister} onLogout={handleLogout}
      />

      <PageHero
        badge="Álbum Oficial" badgeIcon="cards"
        title="Álbum Mundialista 2026"
        subtitle="Colecciona, intercambia y completa la historia del Mundial."
        stats={[
          { num: albumStats.laminasObtenidas || '0', lbl: 'Láminas obtenidas'   },
          { num: `${pct}%`,                          lbl: 'Completado'           },
          { num: repetidas.length  || '0',           lbl: 'Repetidas'            },
          { num: intercambios.length || '0',         lbl: 'Intercambios'         },
        ]}
        mediaKey="album"
      />

      <main className="section">
        <div className="container">

          {/* ── Tabs ── */}
          <div className="alb-tabs">
            {TABS.map(t => (
              <button
                key={t.id}
                className={`alb-tab${tab === t.id ? ' active' : ''}`}
                onClick={() => setTab(t.id)}
              >
                <Icon name={t.icon} size={14} />
                {t.label}
                {t.id === 'intercambios' && intercambios.length > 0 && (
                  <span className="alb-tab-badge">{intercambios.length}</span>
                )}
              </button>
            ))}
          </div>

          <div className="alb-tab-content">

            {/* ────────── Mi Colección ────────── */}
            {tab === 'coleccion' && (
              <div>
                <div className="alb-toolbar">
                  <div className="alb-search-box">
                    <Icon name="search" size={15} />
                    <input
                      className="alb-search-input"
                      placeholder="Buscar jugador, selección…"
                      value={search}
                      onChange={e => setSearch(e.target.value)}
                    />
                    {search && (
                      <button className="alb-search-clear" onClick={() => setSearch('')}>
                        <Icon name="close" size={12} />
                      </button>
                    )}
                  </div>

                  <div className="alb-col-filters">
                    {COL_FILTER.map(f => (
                      <button
                        key={f.id}
                        className={`alb-col-filter${colFilter === f.id ? ' active' : ''}`}
                        onClick={() => setColFilter(f.id)}
                      >
                        {f.label}
                      </button>
                    ))}
                  </div>

                  {allLaminas.length > 0 && (
                    <span className="alb-coleccion-count">
                      {filteredLaminas.length} de {allLaminas.length}
                    </span>
                  )}
                </div>

                {loading ? (
                  <SkeletonGrid />
                ) : allLaminas.length === 0 ? (
                  <EmptyState
                    icon="cards"
                    title="El álbum aún no tiene láminas"
                    sub="Cuando se carguen los jugadores al sistema, aparecerán aquí para coleccionar."
                  />
                ) : filteredLaminas.length === 0 ? (
                  <EmptyState
                    icon="search"
                    title="Sin resultados"
                    sub="No hay láminas que coincidan con tu búsqueda."
                    action={
                      <button className="btn btn-ghost" onClick={() => { setSearch(''); setColFilter('todas'); }}>
                        Limpiar filtros
                      </button>
                    }
                  />
                ) : (
                  <>
                    {!loggedIn && (
                      <div className="alb-guest-notice">
                        <Icon name="shield" size={14} />
                        Inicia sesión para ver tu colección y abrir sobres.
                        <button className="alb-guest-login" onClick={openLogin}>Entrar</button>
                      </div>
                    )}
                    <div className="alb-grid">
                      {filteredLaminas.map(l => (
                        <StickerSlot key={l.id} lamina={l} owned={myLaminaIds.has(l.id)} />
                      ))}
                    </div>
                  </>
                )}
              </div>
            )}

            {/* ────────── Abrir Sobres ────────── */}
            {tab === 'sobres' && (
              <PackCard
                onOpen={handleAbrirSobre}
                opening={opening}
                loggedIn={loggedIn}
                onLogin={openLogin}
              />
            )}

            {/* ────────── Repetidas ────────── */}
            {tab === 'repetidas' && (
              !loggedIn ? <AuthRequired onLogin={openLogin} /> : (
                <div>
                  <div className="alb-section-head">
                    <div>
                      <h2 className="alb-section-title">Láminas Repetidas</h2>
                      <p className="alb-section-sub">Duplicados disponibles para intercambiar.</p>
                    </div>
                    {repetidas.length > 0 && (
                      <span className="alb-count-badge">{repetidas.length}</span>
                    )}
                  </div>

                  {loading ? (
                    <div className="alb-rep-list">
                      {[...Array(4)].map((_, i) => (
                        <div key={i} className="alb-rep-row skel-pulse" style={{ height: 60 }} />
                      ))}
                    </div>
                  ) : repetidas.length === 0 ? (
                    <EmptyState
                      icon="refresh"
                      title="No tienes repetidas aún"
                      sub="Cuando obtengas láminas duplicadas aparecerán aquí para intercambiarlas."
                    />
                  ) : (
                    <div className="alb-rep-list">
                      {repetidas.map((lu, i) => (
                        <RepetidaRow
                          key={lu.id || i}
                          laminaUsuario={lu}
                          lamina={laminaMap.get(lu.idLamina)}
                        />
                      ))}
                    </div>
                  )}
                </div>
              )
            )}

            {/* ────────── Intercambios ────────── */}
            {tab === 'intercambios' && (
              !loggedIn ? <AuthRequired onLogin={openLogin} /> : (
                <div>
                  <div className="alb-section-head">
                    <div>
                      <h2 className="alb-section-title">Intercambios</h2>
                      <p className="alb-section-sub">Solicitudes pendientes donde eres receptor.</p>
                    </div>
                    <button
                      className="btn btn-primary"
                      disabled={repetidas.length === 0}
                      title={repetidas.length === 0 ? 'Necesitas láminas repetidas para ofertar' : 'Crear nueva oferta'}
                      onClick={() => setNuevaOfertaOpen(true)}
                    >
                      <Icon name="plus" size={14} /> Nueva oferta
                    </button>
                  </div>

                  {intercambios.length === 0 ? (
                    <EmptyState
                      icon="users"
                      title="Sin intercambios pendientes"
                      sub="Aquí verás las solicitudes de intercambio que otros usuarios te envíen."
                    />
                  ) : (
                    <div className="alb-int-list">
                      {intercambios.map((int, i) => (
                        <IntercambioRow
                          key={int.id || i}
                          intercambio={int}
                          onAceptar={handleAceptarIntercambio}
                          onRechazar={handleRechazarIntercambio}
                        />
                      ))}
                    </div>
                  )}
                </div>
              )
            )}

            {/* ────────── Progreso ────────── */}
            {tab === 'progreso' && (
              !loggedIn ? <AuthRequired onLogin={openLogin} /> : (
                <div className="alb-prog">
                  <div className="alb-prog-overview">
                    <ProgressRing pct={pct} size={160} stroke={12} />
                    <div className="alb-prog-stats">
                      <div className="alb-prog-stat">
                        <span className="alb-prog-stat-num">{albumStats.laminasObtenidas}</span>
                        <span className="alb-prog-stat-lbl">Obtenidas</span>
                      </div>
                      <div className="alb-prog-stat-div" />
                      <div className="alb-prog-stat">
                        <span className="alb-prog-stat-num">{albumStats.totalLaminas}</span>
                        <span className="alb-prog-stat-lbl">Total</span>
                      </div>
                      <div className="alb-prog-stat-div" />
                      <div className="alb-prog-stat">
                        <span className="alb-prog-stat-num">{repetidas.length}</span>
                        <span className="alb-prog-stat-lbl">Repetidas</span>
                      </div>
                      <div className="alb-prog-stat-div" />
                      <div className="alb-prog-stat">
                        <span className="alb-prog-stat-num">{intercambios.length}</span>
                        <span className="alb-prog-stat-lbl">Intercambios</span>
                      </div>
                    </div>
                  </div>

                  <div className="alb-prog-sections">
                    <div className="alb-prog-section">
                      <h3 className="alb-prog-section-title">Por posición</h3>
                      {Object.entries(categoriaCounts)
                        .sort(([a], [b]) => a.localeCompare(b))
                        .map(([cat, v]) => (
                          <ProgressBar
                            key={cat}
                            label={cat}
                            value={v.owned}
                            total={v.total}
                            color={CATEGORIA_COLOR[cat] || 'var(--accent)'}
                          />
                        ))
                      }
                      {Object.keys(categoriaCounts).length === 0 && (
                        <p style={{ fontSize: 13, color: 'var(--ink-3)' }}>
                          Disponible cuando se carguen las láminas.
                        </p>
                      )}
                    </div>

                    <div className="alb-prog-section">
                      <h3 className="alb-prog-section-title">Por rareza</h3>
                      {Object.entries(rarezaCounts)
                        .filter(([, v]) => v.total > 0)
                        .map(([r, v]) => (
                          <ProgressBar
                            key={r}
                            label={RAREZAS[r].label}
                            value={v.owned}
                            total={v.total}
                            color={RAREZAS[r].color}
                          />
                        ))
                      }
                      {Object.values(rarezaCounts).every(v => v.total === 0) && (
                        <p style={{ fontSize: 13, color: 'var(--ink-3)' }}>
                          Disponible cuando se carguen las láminas.
                        </p>
                      )}
                    </div>
                  </div>

                  <div className="alb-prog-soon">
                    <div className="alb-prog-soon-icon">
                      <Icon name="sparkles" size={28} />
                    </div>
                    <div>
                      <p className="alb-prog-soon-title">Desglose por selección próximamente</p>
                      <p className="alb-prog-soon-sub">
                        Podrás ver el progreso de cada selección clasificada al Mundial 2026.
                      </p>
                    </div>
                  </div>
                </div>
              )
            )}

          </div>
        </div>
      </main>

      <Footer />

      {/* Pack reveal overlay */}
      {revealCards && (
        <PackReveal
          laminas={revealCards.laminas}
          prevIds={revealCards.prevIds}
          onClose={() => { setRevealCards(null); setTab('coleccion'); }}
        />
      )}

      {nuevaOfertaOpen && (
        <NuevaOfertaModal
          repetidas={repetidas}
          laminaMap={laminaMap}
          onClose={() => setNuevaOfertaOpen(false)}
          onSubmit={handleSolicitarIntercambio}
          loading={enviandoOferta}
        />
      )}

      {modal === 'login' && (
        <LoginModal onClose={closeModal} onLoginSuccess={handleLoginSuccess} onSwitchToRegister={openRegister} />
      )}
      {modal === 'register' && (
        <RegisterModal onClose={closeModal} onRegisterSuccess={handleRegisterSuccess} onSwitchToLogin={openLogin} />
      )}
    </div>
  );
};

export default Album;
