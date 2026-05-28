import React, { useState, useEffect, useCallback } from 'react';
import Navbar from '../components/layout/Navbar';
import Footer from '../components/layout/Footer';
import Icon from '../components/ui/Icon';
import LoginModal from '../components/auth/LoginModal';
import PageHero from '../components/common/PageHero';
import RegisterModal from '../components/auth/RegisterModal';
import useDarkMode from '../hooks/useDarkMode';
import useAuth from '../hooks/useAuth';
import {
  getAllPollas, getPollasByMiembro, getMiembros, getRanking,
  crearPolla, unirseAPolla, cerrarPolla,
  getPronosticosByPolla, registrarPronostico, getProximosPartidos,
  getUsuarioById,
} from '../services/pollaService';

// ─── Helpers ──────────────────────────────────────────────────────────────────
const fmtDate = (d) => {
  if (!d) return '–';
  return new Date(d).toLocaleDateString('es-CO', { day: '2-digit', month: 'short', year: 'numeric' });
};
const fmtDateTime = (d) => {
  if (!d) return '';
  const f = new Date(d);
  return f.toLocaleDateString('es', { weekday: 'short', day: 'numeric', month: 'short' }) +
    ' · ' + f.toLocaleTimeString('es', { hour: '2-digit', minute: '2-digit' });
};
const capitalize = (s) => s ? s.charAt(0).toUpperCase() + s.slice(1).toLowerCase() : '';

// ─── Estado badge ─────────────────────────────────────────────────────────────
const EstadoBadge = ({ estado }) => (
  <span className={`pollas-estado-badge ${estado === 'ACTIVA' ? 'activa' : 'cerrada'}`}>
    <span className="pollas-estado-dot" />
    {estado}
  </span>
);

// ─── Skeleton card ────────────────────────────────────────────────────────────
const SkeletonCard = () => (
  <div className="pollas-card pollas-card-skel" aria-hidden="true">
    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 14 }}>
      <div className="skel-line skel-pulse" style={{ width: 72, height: 22, borderRadius: 999 }} />
      <div className="skel-line skel-pulse" style={{ width: 88, height: 22, borderRadius: 8 }} />
    </div>
    <div className="skel-line skel-pulse" style={{ width: '70%', height: 22, marginBottom: 10, borderRadius: 6 }} />
    <div className="skel-line skel-pulse" style={{ width: '90%', height: 14, marginBottom: 6, borderRadius: 4 }} />
    <div className="skel-line skel-pulse" style={{ width: '60%', height: 14, marginBottom: 24, borderRadius: 4 }} />
    <div style={{ display: 'flex', gap: 8 }}>
      <div className="skel-line skel-pulse" style={{ flex: 1, height: 38, borderRadius: 999 }} />
      <div className="skel-line skel-pulse" style={{ width: 80, height: 38, borderRadius: 999 }} />
    </div>
  </div>
);

// ─── Empty state ──────────────────────────────────────────────────────────────
const EmptyState = ({ icon = 'trophy', title, sub, action }) => (
  <div className="pollas-empty">
    <div className="pollas-empty-icon">
      <Icon name={icon} size={40} />
    </div>
    <p className="pollas-empty-title">{title}</p>
    <p className="pollas-empty-sub">{sub}</p>
    {action}
  </div>
);

// ─── Auth required state ──────────────────────────────────────────────────────
const AuthRequired = ({ onLogin, msg = 'Inicia sesión para acceder a esta sección.' }) => (
  <div className="pollas-auth-required">
    <div className="pollas-auth-icon">
      <Icon name="shield" size={36} />
    </div>
    <p className="pollas-auth-title">Acceso restringido</p>
    <p className="pollas-auth-sub">{msg}</p>
    <button className="btn btn-primary btn-lg" onClick={onLogin}>
      Iniciar sesión
    </button>
  </div>
);

// ─── Polla Card ───────────────────────────────────────────────────────────────
const PollaCard = ({ polla, onVerRanking, onVerPronosticos, onUnirse, isMember, isCreator, onCerrar }) => {
  const [miembros, setMiembros] = useState(null);

  useEffect(() => {
    getMiembros(polla.id)
      .then(res => setMiembros(Array.isArray(res.data) ? res.data.length : 0))
      .catch(() => setMiembros(0));
  }, [polla.id]);

  return (
    <article className={`pollas-card ${polla.estado === 'CERRADA' ? 'pollas-card-cerrada' : ''}`}>
      <div className="pollas-card-head">
        <EstadoBadge estado={polla.estado} />
        <span className="pollas-codigo" title="Código de invitación">
          <Icon name="shield" size={10} />
          {polla.codigoInvitacion}
        </span>
      </div>

      <h3 className="pollas-card-name">{polla.nombre}</h3>
      {polla.descripcion && (
        <p className="pollas-card-desc">{polla.descripcion}</p>
      )}

      <div className="pollas-card-meta-row">
        <span className="pollas-meta-chip">
          <Icon name="users" size={11} />
          {miembros === null ? '…' : miembros} {miembros === 1 ? 'miembro' : 'miembros'}
        </span>
        <span className="pollas-meta-chip">
          <Icon name="cal" size={11} />
          {fmtDate(polla.fechaCreacion)}
        </span>
        {isCreator && <span className="pollas-meta-chip creator">Tu polla</span>}
        {isMember && !isCreator && <span className="pollas-meta-chip member">Eres miembro</span>}
      </div>

      <div className="pollas-card-footer">
        <button className="pollas-btn-secondary" onClick={() => onVerRanking(polla)}>
          <Icon name="trophy" size={13} />
          Ranking
        </button>
        {isMember && (
          <button className="pollas-btn-ghost" onClick={() => onVerPronosticos(polla)}>
            <Icon name="star" size={13} />
            Pronósticos
          </button>
        )}
        {polla.estado === 'ACTIVA' && !isMember && (
          <button className="pollas-btn-primary" onClick={() => onUnirse(polla)}>
            Unirse →
          </button>
        )}
        {isCreator && polla.estado === 'ACTIVA' && (
          <button className="pollas-btn-danger" onClick={() => onCerrar(polla)} title="Cerrar polla">
            <Icon name="close" size={12} />
          </button>
        )}
      </div>
    </article>
  );
};

// ─── Ranking table ────────────────────────────────────────────────────────────
const RankingTable = ({ ranking, loading, polla, userId, usuarios }) => {
  if (loading) return (
    <div className="pollas-ranking-loading">
      <div className="spinner" />
      <span>Cargando ranking…</span>
    </div>
  );

  const sorted = [...ranking].sort((a, b) => (b.puntajeTotal || 0) - (a.puntajeTotal || 0));

  return (
    <div className="pollas-ranking-wrap">
      <div className="pollas-ranking-header">
        <div className="pollas-ranking-polla-name">{polla?.nombre}</div>
        <span className={`pollas-estado-badge ${polla?.estado === 'ACTIVA' ? 'activa' : 'cerrada'}`}>
          <span className="pollas-estado-dot" />
          {polla?.estado}
        </span>
      </div>

      {sorted.length === 0 ? (
        <EmptyState icon="users" title="Sin participantes aún" sub="Sé el primero en unirte a esta polla." />
      ) : (
        <div className="pollas-ranking-table-wrap">
          <table className="pollas-ranking-table">
            <thead>
              <tr>
                <th className="col-pos">#</th>
                <th className="col-usuario">Usuario</th>
                <th className="col-pts">Puntaje</th>
                <th className="col-fecha">Desde</th>
              </tr>
            </thead>
            <tbody>
              {sorted.map((m, i) => {
                const pos = m.posicionRanking || (i + 1);
                const isMe = m.idUsuario === userId;
                return (
                  <tr key={m.id} className={`pollas-rank-row ${isMe ? 'is-me' : ''}`}>
                    <td className="col-pos">
                      <span className={`pollas-pos-badge ${pos === 1 ? 'gold' : pos === 2 ? 'silver' : pos === 3 ? 'bronze' : ''}`}>
                        {pos <= 3 ? ['🥇', '🥈', '🥉'][pos - 1] : pos}
                      </span>
                    </td>
                    <td className="col-usuario">
                      <div className="pollas-rank-user">
                        <div className="pollas-rank-avatar">
                          {(usuarios[m.idUsuario]?.[0] || String(m.idUsuario).slice(-1)).toUpperCase()}
                        </div>
                        <span>
                          {usuarios[m.idUsuario] || `Usuario #${m.idUsuario}`}
                          {isMe && <span className="pollas-me-badge">Tú</span>}
                        </span>
                      </div>
                    </td>
                    <td className="col-pts">
                      <span className="pollas-pts-val">{m.puntajeTotal ?? 0}</span>
                      <span className="pollas-pts-lbl"> pts</span>
                    </td>
                    <td className="col-fecha">{fmtDate(m.fechaUnion)}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {polla && (
        <div className="pollas-ranking-footer">
          <span className="pollas-codigo-chip">
            <Icon name="shield" size={11} />
            Código: <strong>{polla.codigoInvitacion}</strong>
          </span>
          <span className="pollas-meta-chip">
            <Icon name="cal" size={11} />
            Creada {fmtDate(polla.fechaCreacion)}
          </span>
        </div>
      )}
    </div>
  );
};

// ─── Pronostico card ──────────────────────────────────────────────────────────
const PronosticoCard = ({ partido, pronostico, onGuardar, saving }) => {
  const isCerrado = pronostico?.cerrado === 1;
  const [local, setLocal] = useState(pronostico?.golesLocalPredichos ?? '');
  const [visita, setVisita] = useState(pronostico?.golesVisitantePredichos ?? '');

  useEffect(() => {
    setLocal(pronostico?.golesLocalPredichos ?? '');
    setVisita(pronostico?.golesVisitantePredichos ?? '');
  }, [pronostico]);

  const handleNum = (setter) => (e) => {
    const v = e.target.value;
    if (v === '' || (/^\d$/.test(v) && Number(v) <= 9)) setter(v);
  };

  const puntaje = pronostico?.puntajeObtenido;

  return (
    <div className={`pollas-prono-card ${isCerrado ? 'cerrado' : ''}`}>
      {isCerrado && (
        <div className="pollas-prono-cerrado-bar">
          <Icon name="shield" size={12} />
          Pronóstico cerrado
          {puntaje !== null && puntaje !== undefined && (
            <span className="pollas-prono-pts-badge">+{puntaje} pts</span>
          )}
        </div>
      )}

      <div className="pollas-prono-header">
        <span className="pollas-prono-fase">{partido.fase || 'Grupo'}</span>
        <span className="pollas-prono-fecha">{fmtDateTime(partido.fechaHora)}</span>
      </div>

      <div className="pollas-prono-match">
        <div className="pollas-prono-team local">
          <span className="pollas-prono-team-name">{partido.equipoLocal}</span>
        </div>

        <div className="pollas-prono-score-area">
          {isCerrado ? (
            <div className="pollas-prono-score-locked">
              <span>{local !== '' ? local : '–'}</span>
              <span className="pollas-prono-dash">:</span>
              <span>{visita !== '' ? visita : '–'}</span>
            </div>
          ) : (
            <div className="pollas-prono-inputs">
              <input
                className="pollas-score-input"
                type="text"
                inputMode="numeric"
                value={local}
                onChange={handleNum(setLocal)}
                placeholder="–"
                maxLength={1}
              />
              <span className="pollas-prono-dash">:</span>
              <input
                className="pollas-score-input"
                type="text"
                inputMode="numeric"
                value={visita}
                onChange={handleNum(setVisita)}
                placeholder="–"
                maxLength={1}
              />
            </div>
          )}
        </div>

        <div className="pollas-prono-team visitante">
          <span className="pollas-prono-team-name">{partido.equipoVisitante}</span>
        </div>
      </div>

      {partido.sede?.nombreEstadio && (
        <div className="pollas-prono-venue">
          <Icon name="pin" size={11} />
          {partido.sede.nombreEstadio}{partido.sede?.ciudad ? `, ${partido.sede.ciudad}` : ''}
        </div>
      )}

      {!isCerrado && (
        <button
          className="pollas-prono-save-btn"
          onClick={() => onGuardar(partido.id, Number(local) || 0, Number(visita) || 0)}
          disabled={saving || local === '' || visita === ''}
        >
          {saving ? 'Guardando…' : pronostico ? 'Actualizar pronóstico' : 'Guardar pronóstico'}
        </button>
      )}
    </div>
  );
};

// ─── Crear Polla Modal ────────────────────────────────────────────────────────
const CrearPollaModal = ({ onClose, onCreate }) => {
  const [form, setForm] = useState({ nombre: '', descripcion: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const h = (e) => e.key === 'Escape' && onClose();
    document.addEventListener('keydown', h);
    document.body.style.overflow = 'hidden';
    return () => { document.removeEventListener('keydown', h); document.body.style.overflow = ''; };
  }, [onClose]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.nombre.trim()) { setError('El nombre de la polla es requerido.'); return; }
    setLoading(true);
    setError('');
    try {
      await onCreate(form.nombre.trim(), form.descripcion.trim());
      onClose();
    } catch (err) {
      setError(err.message || 'Error creando la polla.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal pollas-create-modal" onClick={e => e.stopPropagation()}>
        <button className="modal-close" onClick={onClose} aria-label="Cerrar">
          <Icon name="close" size={18} />
        </button>
        <div className="pollas-modal-icon">
          <Icon name="trophy" size={28} />
        </div>
        <h2>Crear nueva polla</h2>
        <p className="modal-desc">Invita a tus amigos y compite prediciendo los resultados del Mundial 2026.</p>

        {error && <div className="modal-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <label className="input-label">Nombre de la polla</label>
            <input
              className="input"
              placeholder="Ej: Los Campeones del Trabajo"
              value={form.nombre}
              onChange={e => setForm(f => ({ ...f, nombre: e.target.value }))}
              maxLength={80}
              autoFocus
            />
          </div>
          <div className="input-group">
            <label className="input-label">Descripción (opcional)</label>
            <textarea
              className="input pref-textarea"
              placeholder="Describe tu polla…"
              value={form.descripcion}
              onChange={e => setForm(f => ({ ...f, descripcion: e.target.value }))}
              rows={3}
              maxLength={255}
              style={{ resize: 'none' }}
            />
          </div>

          <div className="pollas-modal-hint">
            <Icon name="info" size={13} />
            El código de invitación se genera automáticamente.
          </div>

          <button type="submit" className="modal-btn" disabled={loading || !form.nombre.trim()}>
            {loading ? 'Creando…' : 'Crear polla'}
          </button>
        </form>
      </div>
    </div>
  );
};

// ─── Join modal ───────────────────────────────────────────────────────────────
const JoinModal = ({ onClose, onJoin, preCode = '' }) => {
  const [code, setCode] = useState(preCode);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const h = (e) => e.key === 'Escape' && onClose();
    document.addEventListener('keydown', h);
    document.body.style.overflow = 'hidden';
    return () => { document.removeEventListener('keydown', h); document.body.style.overflow = ''; };
  }, [onClose]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!code.trim()) { setError('Ingresa el código de invitación.'); return; }
    setLoading(true);
    setError('');
    try {
      await onJoin(code.trim().toUpperCase());
      onClose();
    } catch (err) {
      setError(err.message || 'Código inválido o ya eres miembro.');
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal pollas-create-modal" onClick={e => e.stopPropagation()}>
        <button className="modal-close" onClick={onClose} aria-label="Cerrar">
          <Icon name="close" size={18} />
        </button>
        <div className="pollas-modal-icon">
          <Icon name="users" size={28} />
        </div>
        <h2>Unirse a una polla</h2>
        <p className="modal-desc">Ingresa el código de invitación que te compartieron.</p>

        {error && <div className="modal-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <label className="input-label">Código de invitación</label>
            <input
              className="input"
              placeholder="Ej: A1B2C3D4"
              value={code}
              onChange={e => setCode(e.target.value.toUpperCase())}
              maxLength={8}
              autoFocus
              style={{ fontFamily: 'var(--font-mono)', letterSpacing: '0.1em', fontSize: 18, textAlign: 'center' }}
            />
          </div>
          <button type="submit" className="modal-btn" disabled={loading || !code.trim()}>
            {loading ? 'Uniéndose…' : 'Unirse a la polla'}
          </button>
        </form>
      </div>
    </div>
  );
};

// ─── Main page ────────────────────────────────────────────────────────────────
const Pollas = () => {
  const { dark, toggleDark } = useDarkMode();
  const {
    loggedIn, user, modal, setToast,
    openLogin, openRegister, closeModal,
    handleLoginSuccess, handleRegisterSuccess, handleLogout,
  } = useAuth();

  const userId = user?.usuario?.id || user?.id;

  // ── Data ──────────────────────────────────────────────────────────────────
  const [allPollas, setAllPollas]     = useState([]);
  const [misPollas, setMisPollas]     = useState([]);
  const [selectedPolla, setSelectedPolla] = useState(null);
  const [ranking, setRanking]         = useState([]);
  const [usuarios, setUsuarios]       = useState({});
  const [partidos, setPartidos]       = useState([]);
  const [pronosticos, setPronosticos] = useState([]);

  // ── Loading ───────────────────────────────────────────────────────────────
  const [loadingAll, setLoadingAll]         = useState(true);
  const [loadingMias, setLoadingMias]       = useState(false);
  const [loadingRanking, setLoadingRanking] = useState(false);
  const [loadingPartidos, setLoadingPartidos] = useState(false);

  // ── UI ─────────────────────────────────────────────────────────────────────
  const [tab, setTab]           = useState('explorar');
  const [busqueda, setBusqueda] = useState('');
  const [filtroEstado, setFiltroEstado] = useState('TODAS');
  const [createOpen, setCreateOpen] = useState(false);
  const [joinOpen, setJoinOpen]     = useState(false);
  const [joinPreCode, setJoinPreCode] = useState('');
  const [savingProno, setSavingProno] = useState(false);
  const [joinCodeInline, setJoinCodeInline] = useState('');
  const [joiningInline, setJoiningInline]   = useState(false);
  const [joinError, setJoinError]   = useState('');

  // ── Load all pollas ────────────────────────────────────────────────────────
  const reloadAll = useCallback(() => {
    setLoadingAll(true);
    getAllPollas()
      .then(r => setAllPollas(Array.isArray(r.data) ? r.data : []))
      .catch(() => setAllPollas([]))
      .finally(() => setLoadingAll(false));
  }, []);

  useEffect(() => { reloadAll(); }, [reloadAll]);

  // ── Load mis pollas ────────────────────────────────────────────────────────
  const reloadMias = useCallback(() => {
    if (!loggedIn || !userId) { setMisPollas([]); return; }
    setLoadingMias(true);
    getPollasByMiembro(userId)
      .then(r => setMisPollas(Array.isArray(r.data) ? r.data : []))
      .catch(() => setMisPollas([]))
      .finally(() => setLoadingMias(false));
  }, [loggedIn, userId]);

  useEffect(() => { reloadMias(); }, [reloadMias]);

  // ── Load ranking when polla selected ──────────────────────────────────────
  useEffect(() => {
    if (!selectedPolla) return;
    setLoadingRanking(true);
    setUsuarios({});
    getRanking(selectedPolla.id)
      .then(async r => {
        const rankData = Array.isArray(r.data) ? r.data : [];
        setRanking(rankData);
        const uniqueIds = [...new Set(rankData.map(m => m.idUsuario))];
        const results = await Promise.all(
          uniqueIds.map(id =>
            getUsuarioById(id)
              .then(res => ({ id, data: res.data }))
              .catch(() => ({ id, data: null }))
          )
        );
        const map = {};
        results.forEach(({ id, data }) => {
          if (data) {
            const capitalize = (s) => s ? s.charAt(0).toUpperCase() + s.slice(1).toLowerCase() : '';
            map[id] = [capitalize(data.nombres), capitalize(data.apellidos)].filter(Boolean).join(' ');
          }
        });
        setUsuarios(map);
      })
      .catch(() => setRanking([]))
      .finally(() => setLoadingRanking(false));
  }, [selectedPolla]);

  // ── Load partidos + pronósticos when pronosticos tab ──────────────────────
  useEffect(() => {
    if (tab !== 'pronosticos' || !selectedPolla) return;
    setLoadingPartidos(true);
    const pronosLoad = loggedIn && userId
      ? getPronosticosByPolla(selectedPolla.id)
      : Promise.resolve({ data: [] });

    Promise.all([getProximosPartidos(), pronosLoad])
      .then(([partRes, pronosRes]) => {
        setPartidos(Array.isArray(partRes.data) ? partRes.data : []);
        const mis = Array.isArray(pronosRes.data)
          ? pronosRes.data.filter(p => p.idUsuario === userId)
          : [];
        setPronosticos(mis);
      })
      .catch(() => {})
      .finally(() => setLoadingPartidos(false));
  }, [tab, selectedPolla, loggedIn, userId]);

  // ── Derived ───────────────────────────────────────────────────────────────
  const misPollasIds = new Set(misPollas.map(p => p.id));

  const filteredPollas = allPollas
    .filter(p => filtroEstado === 'TODAS' || p.estado === filtroEstado)
    .filter(p => {
      if (!busqueda) return true;
      const q = busqueda.toLowerCase();
      return p.nombre?.toLowerCase().includes(q) || p.descripcion?.toLowerCase().includes(q);
    });

  const statsHero = [
    { num: loadingAll ? '…' : allPollas.filter(p => p.estado === 'ACTIVA').length.toString(), lbl: 'Pollas activas' },
    { num: loadingAll ? '…' : allPollas.length.toString(), lbl: 'Total pollas' },
    { num: loggedIn ? (loadingMias ? '…' : misPollas.length.toString()) : '–', lbl: 'Mis pollas' },
    { num: '5', lbl: 'Pts. marcador exacto' },
  ];

  // ── Actions ───────────────────────────────────────────────────────────────
  const handleVerRanking = (polla) => {
    setSelectedPolla(polla);
    setTab('ranking');
  };

  const handleVerPronosticos = (polla) => {
    setSelectedPolla(polla);
    setTab('pronosticos');
  };

  const handleUnirseCard = (polla) => {
    if (!loggedIn) { openLogin(); return; }
    setJoinPreCode(polla.codigoInvitacion);
    setJoinOpen(true);
  };

  const handleCrear = async (nombre, descripcion) => {
    if (!loggedIn) { openLogin(); throw new Error('no auth'); }
    const res = await crearPolla({ nombre, descripcion, idCreador: userId });
    const codigo = res.data?.codigoInvitacion || res.data?.polla?.codigoInvitacion;
    const pollaNombre = res.data?.polla?.nombre || nombre;
    setToast(`¡Polla "${pollaNombre}" creada! Código: ${codigo}`);
    reloadAll();
    reloadMias();
  };

  const handleUnirse = async (codigo) => {
    const res = await unirseAPolla(codigo, userId);
    setToast(res.data?.mensaje || `¡Te uniste a la polla!`);
    reloadAll();
    reloadMias();
  };

  const handleUnirseInline = async () => {
    if (!joinCodeInline.trim()) { setJoinError('Ingresa un código.'); return; }
    if (!loggedIn) { openLogin(); return; }
    setJoiningInline(true);
    setJoinError('');
    try {
      const res = await unirseAPolla(joinCodeInline.trim().toUpperCase(), userId);
      setToast(res.data?.mensaje || '¡Te uniste a la polla!');
      setJoinCodeInline('');
      reloadAll();
      reloadMias();
    } catch (e) {
      setJoinError(e.response?.data?.mensaje || 'Código inválido o ya eres miembro.');
    } finally {
      setJoiningInline(false);
    }
  };

  const handleCerrar = async (polla) => {
    if (!window.confirm(`¿Cerrar la polla "${polla.nombre}"? Esta acción no se puede deshacer.`)) return;
    try {
      await cerrarPolla(polla.id);
      setToast(`Polla "${polla.nombre}" cerrada.`);
      reloadAll();
      reloadMias();
      if (selectedPolla?.id === polla.id) setSelectedPolla(prev => ({ ...prev, estado: 'CERRADA' }));
    } catch {
      setToast('Error cerrando la polla.');
    }
  };

  const handleGuardarPronostico = async (idPartido, golesLocal, golesVisitante) => {
    if (!loggedIn || !selectedPolla) return;
    setSavingProno(true);
    let resultado = 'EMPATE';
    if (golesLocal > golesVisitante) resultado = 'LOCAL';
    else if (golesVisitante > golesLocal) resultado = 'VISITANTE';
    try {
      await registrarPronostico({
        idPolla: selectedPolla.id,
        idUsuario: userId,
        idPartido,
        golesLocalPredichos: golesLocal,
        golesVisitantePredichos: golesVisitante,
        resultadoPredicho: resultado,
      });
      setToast('¡Pronóstico guardado!');
      const pronosRes = await getPronosticosByPolla(selectedPolla.id);
      const mis = Array.isArray(pronosRes.data) ? pronosRes.data.filter(p => p.idUsuario === userId) : [];
      setPronosticos(mis);
    } catch (e) {
      setToast(e.response?.data?.mensaje || 'Error guardando pronóstico.');
    } finally {
      setSavingProno(false);
    }
  };

  // ── Tabs config ───────────────────────────────────────────────────────────
  const TABS = [
    { id: 'explorar',     label: 'Explorar',     icon: 'search' },
    { id: 'mis-pollas',   label: 'Mis Pollas',   icon: 'star',    requiresAuth: true },
    { id: 'ranking',      label: 'Ranking',      icon: 'trophy',  requiresPolla: true },
    { id: 'pronosticos',  label: 'Pronósticos',  icon: 'shield',  requiresPolla: true, requiresAuth: true },
  ];

  // ── Render ────────────────────────────────────────────────────────────────
  return (
    <div className="app">
      <Navbar
        dark={dark} onToggleDark={toggleDark}
        loggedIn={loggedIn} user={user}
        onLogin={openLogin} onRegister={openRegister} onLogout={handleLogout}
      />

      <PageHero
        badge="FIFA World Cup 2026™"
        badgeIcon="trophy"
        title={<>POLLAS<br />MUNDIALISTAS</>}
        subtitle="Compite con tus amigos, predice los resultados y sube al ranking. El que más acierta, gana."
        stats={statsHero}
        variant="pollas"
        mediaKey="pollas"
      />

      {/* ── Action bar: Join inline + Create ──────────────────────── */}
      <div className="pollas-action-bar-wrap">
        <div className="container pollas-action-bar">
          <div className="pollas-join-inline">
            <Icon name="shield" size={14} />
            <input
              className="pollas-join-input"
              placeholder="Código de invitación…"
              value={joinCodeInline}
              onChange={e => { setJoinCodeInline(e.target.value.toUpperCase()); setJoinError(''); }}
              onKeyDown={e => e.key === 'Enter' && handleUnirseInline()}
              maxLength={8}
            />
            {joinError && <span className="pollas-join-err">{joinError}</span>}
            <button
              className="btn btn-primary"
              onClick={handleUnirseInline}
              disabled={joiningInline || !joinCodeInline.trim()}
            >
              {joiningInline ? 'Uniéndose…' : 'Unirse'}
            </button>
          </div>

          {loggedIn ? (
            <button className="pollas-create-btn" onClick={() => setCreateOpen(true)}>
              <Icon name="plus" size={15} />
              Crear polla
            </button>
          ) : (
            <button className="pollas-create-btn ghost" onClick={openLogin}>
              <Icon name="plus" size={15} />
              Crear polla
            </button>
          )}
        </div>
      </div>

      {/* ── Tab navigation ────────────────────────────────────────── */}
      <div className="pollas-tabs-bar">
        <div className="container pollas-tabs">
          {TABS.map(t => {
            const isDisabled = (t.requiresPolla && !selectedPolla) || (t.requiresAuth && !loggedIn && !t.requiresPolla);
            return (
              <button
                key={t.id}
                className={`pollas-tab ${tab === t.id ? 'active' : ''} ${isDisabled ? 'disabled' : ''}`}
                onClick={() => {
                  if (isDisabled) {
                    if (t.requiresAuth && !loggedIn) { openLogin(); return; }
                    return;
                  }
                  setTab(t.id);
                }}
                title={t.requiresPolla && !selectedPolla ? 'Selecciona una polla primero' : undefined}
              >
                <Icon name={t.icon} size={14} />
                {t.label}
                {t.id === 'ranking' && selectedPolla && (
                  <span className="pollas-tab-badge">{selectedPolla.nombre.slice(0, 10)}{selectedPolla.nombre.length > 10 ? '…' : ''}</span>
                )}
                {t.id === 'mis-pollas' && loggedIn && misPollas.length > 0 && (
                  <span className="pollas-tab-count">{misPollas.length}</span>
                )}
              </button>
            );
          })}
        </div>
      </div>

      {/* ── Tab content ───────────────────────────────────────────── */}
      <main className="container pollas-main">

        {/* ── EXPLORAR ─────────────────────────────────────────── */}
        {tab === 'explorar' && (
          <div className="pollas-tab-content">
            <div className="pollas-toolbar">
              <div className="pollas-search-wrap">
                <Icon name="search" size={14} />
                <input
                  className="pollas-search"
                  placeholder="Buscar pollas…"
                  value={busqueda}
                  onChange={e => setBusqueda(e.target.value)}
                />
                {busqueda && (
                  <button className="pollas-search-clear" onClick={() => setBusqueda('')}>
                    <Icon name="close" size={13} />
                  </button>
                )}
              </div>
              <div className="pollas-filter-pills">
                {['TODAS', 'ACTIVA', 'CERRADA'].map(f => (
                  <button
                    key={f}
                    className={`pollas-filter-pill ${filtroEstado === f ? 'active' : ''}`}
                    onClick={() => setFiltroEstado(f)}
                  >
                    {f === 'TODAS' ? 'Todas' : capitalize(f)}
                    <span className="pollas-filter-count">
                      {f === 'TODAS' ? allPollas.length : allPollas.filter(p => p.estado === f).length}
                    </span>
                  </button>
                ))}
              </div>
            </div>

            {loadingAll ? (
              <div className="pollas-grid">
                {Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)}
              </div>
            ) : filteredPollas.length === 0 ? (
              <EmptyState
                icon="trophy"
                title="No hay pollas disponibles"
                sub={busqueda ? `Sin resultados para "${busqueda}"` : 'Sé el primero en crear una polla mundialista.'}
                action={
                  <button className="btn btn-primary btn-lg" onClick={() => loggedIn ? setCreateOpen(true) : openLogin()}>
                    <Icon name="plus" size={14} /> Crear polla
                  </button>
                }
              />
            ) : (
              <div className="pollas-grid">
                {filteredPollas.map(p => (
                  <PollaCard
                    key={p.id}
                    polla={p}
                    onVerRanking={handleVerRanking}
                    onVerPronosticos={handleVerPronosticos}
                    onUnirse={handleUnirseCard}
                    onCerrar={handleCerrar}
                    isMember={misPollasIds.has(p.id)}
                    isCreator={p.idCreador === userId}
                  />
                ))}
              </div>
            )}
          </div>
        )}

        {/* ── MIS POLLAS ───────────────────────────────────────── */}
        {tab === 'mis-pollas' && (
          <div className="pollas-tab-content">
            {!loggedIn ? (
              <AuthRequired onLogin={openLogin} msg="Inicia sesión para ver las pollas en las que participas." />
            ) : loadingMias ? (
              <div className="pollas-grid">
                {Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={i} />)}
              </div>
            ) : misPollas.length === 0 ? (
              <EmptyState
                icon="users"
                title="Aún no estás en ninguna polla"
                sub="Únete con un código de invitación o crea tu propia polla mundialista."
                action={
                  <div style={{ display: 'flex', gap: 12, justifyContent: 'center', flexWrap: 'wrap' }}>
                    <button className="btn btn-primary btn-lg" onClick={() => setCreateOpen(true)}>
                      <Icon name="plus" size={14} /> Crear polla
                    </button>
                    <button className="btn btn-ghost btn-lg" onClick={() => setJoinOpen(true)}>
                      <Icon name="arrow-r" size={14} /> Unirse con código
                    </button>
                  </div>
                }
              />
            ) : (
              <div className="pollas-grid">
                {misPollas.map(p => (
                  <PollaCard
                    key={p.id}
                    polla={p}
                    onVerRanking={handleVerRanking}
                    onVerPronosticos={handleVerPronosticos}
                    onUnirse={handleUnirseCard}
                    onCerrar={handleCerrar}
                    isMember={true}
                    isCreator={p.idCreador === userId}
                  />
                ))}
              </div>
            )}
          </div>
        )}

        {/* ── RANKING ──────────────────────────────────────────── */}
        {tab === 'ranking' && (
          <div className="pollas-tab-content">
            {!selectedPolla ? (
              <EmptyState
                icon="trophy"
                title="Selecciona una polla"
                sub='Explora las pollas y haz clic en "Ranking" para ver la clasificación.'
              />
            ) : (
              <RankingTable
                ranking={ranking}
                loading={loadingRanking}
                polla={selectedPolla}
                userId={userId}
                usuarios={usuarios}
              />
            )}
          </div>
        )}

        {/* ── PRONÓSTICOS ──────────────────────────────────────── */}
        {tab === 'pronosticos' && (
          <div className="pollas-tab-content">
            {!loggedIn ? (
              <AuthRequired onLogin={openLogin} msg="Inicia sesión para registrar tus pronósticos." />
            ) : !selectedPolla ? (
              <EmptyState
                icon="star"
                title="Selecciona una polla"
                sub='Elige una polla en la que seas miembro y haz clic en "Pronósticos".'
              />
            ) : !misPollasIds.has(selectedPolla.id) ? (
              <div className="pollas-auth-required">
                <div className="pollas-auth-icon"><Icon name="shield" size={36} /></div>
                <p className="pollas-auth-title">No eres miembro</p>
                <p className="pollas-auth-sub">Únete a esta polla para poder hacer pronósticos.</p>
                <button className="btn btn-primary btn-lg" onClick={() => handleUnirseCard(selectedPolla)}>
                  Unirse a {selectedPolla.nombre}
                </button>
              </div>
            ) : loadingPartidos ? (
              <div className="pollas-loading-state">
                <div className="spinner" />
                <span>Cargando partidos…</span>
              </div>
            ) : partidos.length === 0 ? (
              <EmptyState
                icon="cal"
                title="Sin partidos próximos"
                sub="No hay partidos disponibles para pronosticar en este momento."
              />
            ) : (
              <div className="pollas-prono-section">
                <div className="pollas-prono-context">
                  <div className="pollas-prono-polla-info">
                    <Icon name="trophy" size={16} />
                    <strong>{selectedPolla.nombre}</strong>
                    <EstadoBadge estado={selectedPolla.estado} />
                  </div>
                  <p className="pollas-prono-hint">
                    <Icon name="info" size={12} />
                    3 pts. por resultado correcto · +2 pts. extra por marcador exacto
                  </p>
                </div>

                <div className="pollas-prono-grid">
                  {partidos.map(partido => {
                    const miProno = pronosticos.find(p => p.idPartido === partido.id);
                    return (
                      <PronosticoCard
                        key={partido.id}
                        partido={partido}
                        pronostico={miProno}
                        onGuardar={handleGuardarPronostico}
                        saving={savingProno}
                      />
                    );
                  })}
                </div>
              </div>
            )}
          </div>
        )}
      </main>

      <Footer />

      {/* ── Modals ──────────────────────────────────────────────── */}
      {createOpen && (
        <CrearPollaModal
          onClose={() => setCreateOpen(false)}
          onCreate={handleCrear}
        />
      )}
      {joinOpen && (
        <JoinModal
          onClose={() => { setJoinOpen(false); setJoinPreCode(''); }}
          onJoin={handleUnirse}
          preCode={joinPreCode}
        />
      )}

      {modal === 'login'    && <LoginModal    onClose={closeModal} onLoginSuccess={handleLoginSuccess}       onSwitchToRegister={openRegister} />}
      {modal === 'register' && <RegisterModal onClose={closeModal} onRegisterSuccess={handleRegisterSuccess} onSwitchToLogin={openLogin} />}
    </div>
  );
};

export default Pollas;
