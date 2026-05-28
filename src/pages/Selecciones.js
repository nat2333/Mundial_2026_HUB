import React, { useState, useEffect, useMemo } from 'react';
import { getPartidosParaEquipos, getLaminasPorEquipo } from '../services/seleccionesService';
import TeamFlag from '../components/TeamFlag';
import '../App.css';
import Navbar from '../components/layout/Navbar';
import Footer from '../components/layout/Footer';
import Icon from '../components/ui/Icon';
import PageHero from '../components/common/PageHero';
import LoginModal from '../components/auth/LoginModal';
import RegisterModal from '../components/auth/RegisterModal';
import useAuth from '../hooks/useAuth';
import useDarkMode from '../hooks/useDarkMode';
import { teamIso2 } from '../utils/countries';
import { adaptPhase } from '../utils/phases';
import { formatDate, formatTime } from '../utils/formatters';

// ======= CONFEDERATION MAP =======
const CONF_MAP = {
  MX: 'CONCACAF', CA: 'CONCACAF', US: 'CONCACAF', CR: 'CONCACAF', PA: 'CONCACAF',
  HN: 'CONCACAF', JM: 'CONCACAF', SV: 'CONCACAF', TT: 'CONCACAF', HT: 'CONCACAF',
  CU: 'CONCACAF', GT: 'CONCACAF', NI: 'CONCACAF', CW: 'CONCACAF',
  AR: 'CONMEBOL', BR: 'CONMEBOL', UY: 'CONMEBOL', CO: 'CONMEBOL', PE: 'CONMEBOL',
  CL: 'CONMEBOL', VE: 'CONMEBOL', PY: 'CONMEBOL', BO: 'CONMEBOL', EC: 'CONMEBOL',
  FR: 'UEFA', ES: 'UEFA', DE: 'UEFA', PT: 'UEFA', GB: 'UEFA', NL: 'UEFA', HR: 'UEFA',
  BE: 'UEFA', PL: 'UEFA', DK: 'UEFA', CH: 'UEFA', RS: 'UEFA', AT: 'UEFA', SE: 'UEFA',
  NO: 'UEFA', FI: 'UEFA', IS: 'UEFA', IE: 'UEFA', TR: 'UEFA', GR: 'UEFA', UA: 'UEFA',
  HU: 'UEFA', RO: 'UEFA', CZ: 'UEFA', SK: 'UEFA', SI: 'UEFA', AL: 'UEFA', MK: 'UEFA',
  BA: 'UEFA', ME: 'UEFA', LU: 'UEFA', GE: 'UEFA', AM: 'UEFA', AZ: 'UEFA', BY: 'UEFA',
  MD: 'UEFA', XK: 'UEFA',
  MA: 'CAF', SN: 'CAF', GH: 'CAF', CM: 'CAF', TN: 'CAF', NG: 'CAF', EG: 'CAF',
  ML: 'CAF', CI: 'CAF', AO: 'CAF', MZ: 'CAF', ZW: 'CAF', ZM: 'CAF', TZ: 'CAF',
  UG: 'CAF', NA: 'CAF', GN: 'CAF', BF: 'CAF', GM: 'CAF', KM: 'CAF', CV: 'CAF',
  LY: 'CAF', DZ: 'CAF', ZA: 'CAF', ET: 'CAF', KE: 'CAF', CD: 'CAF', GQ: 'CAF',
  BJ: 'CAF', SD: 'CAF', MR: 'CAF',
  JP: 'AFC', KR: 'AFC', KP: 'AFC', SA: 'AFC', IR: 'AFC', AU: 'AFC', CN: 'AFC',
  QA: 'AFC', AE: 'AFC', IQ: 'AFC', JO: 'AFC', OM: 'AFC', BH: 'AFC', KW: 'AFC',
  SY: 'AFC', UZ: 'AFC', KG: 'AFC', TJ: 'AFC', IN: 'AFC', ID: 'AFC', PH: 'AFC',
  TH: 'AFC', VN: 'AFC', PS: 'AFC',
  NZ: 'OFC',
};
const CONF_COLORS = {
  UEFA:     { bg: 'rgba(48,79,254,.13)',   color: 'var(--accent)',    pill: '#304ffe' },
  CONMEBOL: { bg: 'rgba(0,166,80,.13)',    color: '#00904a',          pill: '#00a650' },
  CONCACAF: { bg: 'rgba(255,107,53,.13)',  color: '#c85020',          pill: '#e05a25' },
  CAF:      { bg: 'rgba(247,183,49,.16)',  color: '#a06a00',          pill: '#f7b731' },
  AFC:      { bg: 'rgba(0,188,212,.13)',   color: '#007a8c',          pill: '#00bcd4' },
  OFC:      { bg: 'rgba(156,39,176,.13)', color: '#7b1fa2',          pill: '#9c27b0' },
};
const getConf = (iso2) => CONF_MAP[iso2] || 'OTHER';
const confStyle = (conf) => CONF_COLORS[conf] || { bg: 'var(--bg-soft)', color: 'var(--ink-3)', pill: '#9094bc' };
const CONF_ORDER = ['UEFA', 'CONMEBOL', 'CONCACAF', 'CAF', 'AFC', 'OFC'];

// ======= DERIVE TEAMS FROM MATCHES =======
const deriveTeams = (partidos) => {
  if (!Array.isArray(partidos) || partidos.length === 0) return [];
  const map = {};

  const addTeam = (name, fase) => {
    if (!name) return;
    if (!map[name]) {
      const iso2 = teamIso2(name);
      map[name] = { name, iso2, conf: getConf(iso2), group: null, pj: 0, gf: 0, gc: 0, pts: 0, w: 0, d: 0, l: 0 };
    }
    if (fase && /^GROUP_[A-L]$/.test(fase) && !map[name].group) {
      map[name].group = fase;
    }
  };

  partidos.forEach((p) => {
    addTeam(p.equipoLocal, p.fase);
    addTeam(p.equipoVisitante, p.fase);

    if (p.estado !== 'FINALIZADO') return;
    const gl = p.golesLocal ?? 0;
    const gv = p.golesVisitante ?? 0;

    if (map[p.equipoLocal]) {
      const t = map[p.equipoLocal];
      t.pj++; t.gf += gl; t.gc += gv;
      if (gl > gv) { t.pts += 3; t.w++; }
      else if (gl === gv) { t.pts += 1; t.d++; }
      else t.l++;
    }
    if (map[p.equipoVisitante]) {
      const t = map[p.equipoVisitante];
      t.pj++; t.gf += gv; t.gc += gl;
      if (gv > gl) { t.pts += 3; t.w++; }
      else if (gv === gl) { t.pts += 1; t.d++; }
      else t.l++;
    }
  });

  return Object.values(map).sort((a, b) => {
    const ga = a.group || 'Z_Z';
    const gb = b.group || 'Z_Z';
    if (ga !== gb) return ga.localeCompare(gb);
    return a.name.localeCompare(b.name);
  });
};

// ======= TEAM CARD SKELETON =======
const TeamCardSkeleton = () => (
  <div className="sel-team-card sel-team-skeleton" aria-hidden="true">
    <div className="sel-skel-flag" />
    <div className="sel-card-body">
      <div className="sel-skel-name" />
      <div className="sel-skel-badges" />
      <div className="sel-skel-stats" />
    </div>
  </div>
);

// ======= TEAM CARD =======
const TeamCard = ({ team, onClick, idx }) => {
  const cc = confStyle(team.conf);
  return (
    <button
      className="sel-team-card"
      onClick={() => onClick(team)}
      style={{ animationDelay: `${Math.min(idx * 0.04, 0.6)}s` }}
    >
      <div className="sel-card-flag-wrap">
        {team.iso2 ? (
          <TeamFlag iso2={team.iso2} size="hero" />
        ) : (
          <div className="sel-flag-placeholder">
            <Icon name="shield" size={32} />
          </div>
        )}
      </div>
      <div className="sel-card-body">
        <div className="sel-card-name">{team.name}</div>
        <div className="sel-card-badges">
          {team.group && (
            <span className="sel-group-badge">{adaptPhase(team.group)}</span>
          )}
          <span className="sel-conf-badge" style={{ background: cc.bg, color: cc.color }}>
            {team.conf === 'OTHER' ? '–' : team.conf}
          </span>
        </div>
        {team.pj > 0 && (
          <div className="sel-card-stats mono">
            <span>{team.pj} <span style={{ color: 'var(--ink-4)' }}>PJ</span></span>
            <span className="sel-stats-sep">·</span>
            <span style={{ color: 'var(--ink)', fontWeight: 700 }}>{team.pts} pts</span>
            <span className="sel-stats-sep">·</span>
            <span>{team.gf}<span style={{ color: 'var(--ink-4)' }}>:</span>{team.gc}</span>
          </div>
        )}
      </div>
      <div className="sel-card-cta">
        Ver detalles <Icon name="arrow" size={11} />
      </div>
    </button>
  );
};

// ======= MINI MATCH ROW (inside detail modal) =======
const MatchMiniRow = ({ p, teamName }) => {
  const isHome = p.equipoLocal === teamName;
  const opponent = isHome ? p.equipoVisitante : p.equipoLocal;
  const oppIso2 = teamIso2(opponent);
  const isDone = p.estado === 'FINALIZADO';
  const isLive = p.estado === 'EN_JUEGO';
  const gl = p.golesLocal ?? 0;
  const gv = p.golesVisitante ?? 0;
  const myGoals = isHome ? gl : gv;
  const oppGoals = isHome ? gv : gl;
  const result = (isDone || isLive)
    ? (myGoals > oppGoals ? 'win' : myGoals < oppGoals ? 'loss' : 'draw')
    : null;

  return (
    <div className={'sel-match-mini' + (isLive ? ' live' : isDone ? ' done' : '')}>
      <div className="sel-mm-left">
        <div className="sel-mm-phase">{adaptPhase(p.fase)}</div>
        <div className="sel-mm-opp">
          <span className="sel-mm-opp-flag">
            <TeamFlag iso2={oppIso2} size="row" />
          </span>
          <span className="sel-mm-opp-name">{opponent || '?'}</span>
          <span className="sel-mm-loc">{isHome ? 'Local' : 'Visit.'}</span>
        </div>
      </div>
      <div className="sel-mm-right">
        {(isDone || isLive) ? (
          <div className={`sel-mm-score ${result || ''}`}>
            {myGoals}–{oppGoals}
            {isLive && <span className="sel-mm-live-dot" />}
          </div>
        ) : (
          <div className="sel-mm-date">
            <span className="mono">{formatDate(p.fechaHora)}</span>
            <span className="sel-mm-time mono">{formatTime(p.fechaHora)}</span>
          </div>
        )}
      </div>
    </div>
  );
};

// ======= TEAM DETAIL MODAL =======
const TeamDetailModal = ({ team, rawPartidos, onClose }) => {
  const [players, setPlayers] = useState([]);
  const [playersLoading, setPlayersLoading] = useState(true);
  const cc = confStyle(team.conf);

  useEffect(() => {
    getLaminasPorEquipo(team.name)
      .then((res) => setPlayers(Array.isArray(res?.data) ? res.data : []))
      .catch(() => setPlayers([]))
      .finally(() => setPlayersLoading(false));
  }, [team.name]);

  useEffect(() => {
    const h = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', h);
    return () => document.removeEventListener('keydown', h);
  }, [onClose]);

  const teamMatches = rawPartidos
    .filter((p) => p.equipoLocal === team.name || p.equipoVisitante === team.name)
    .sort((a, b) => new Date(a.fechaHora || 0) - new Date(b.fechaHora || 0));

  const upcoming = teamMatches.filter((p) => p.estado === 'PROGRAMADO' || p.estado === 'POSPUESTO');
  const finished = teamMatches.filter((p) => p.estado === 'FINALIZADO' || p.estado === 'EN_JUEGO')
    .slice().reverse().slice(0, 5);

  const stats = [
    { lbl: 'PJ', val: team.pj },
    { lbl: 'V',  val: team.w  },
    { lbl: 'E',  val: team.d  },
    { lbl: 'D',  val: team.l  },
    { lbl: 'GF', val: team.gf },
    { lbl: 'GC', val: team.gc },
    { lbl: 'PTS', val: team.pts, accent: true },
  ];

  return (
    <div className="sel-modal-overlay" onClick={onClose}>
      <div className="sel-modal" onClick={(e) => e.stopPropagation()}>
        <button className="sel-modal-close" onClick={onClose} title="Cerrar">
          <Icon name="close" size={16} />
        </button>

        {/* ── Hero ── */}
        <div className="sel-modal-hero">
          <div className="sel-modal-hero-bg" />
          <div className="sel-modal-hero-inner">
            <div className="sel-modal-hero-flag">
              {team.iso2 ? (
                <TeamFlag
                  iso2={team.iso2}
                  style={{ width: '96px', height: '64px', borderRadius: '8px', display: 'block', objectFit: 'cover' }}
                />
              ) : (
                <div className="sel-modal-flag-ph">
                  <Icon name="shield" size={36} />
                </div>
              )}
            </div>
            <div className="sel-modal-hero-info">
              <h2 className="sel-modal-team-name">{team.name}</h2>
              <div className="sel-modal-meta">
                <span
                  className="sel-modal-conf-badge"
                  style={{ background: 'rgba(255,255,255,.15)', color: '#fff' }}
                >
                  {team.conf === 'OTHER' ? 'Sin conf.' : team.conf}
                </span>
                {team.group && (
                  <span className="sel-modal-group-badge">
                    {adaptPhase(team.group)}
                  </span>
                )}
              </div>
            </div>
          </div>

          {/* Stats strip */}
          <div className="sel-modal-stats-strip">
            {stats.map(({ lbl, val, accent }) => (
              <div key={lbl} className="sel-modal-stat">
                <div className={`sel-modal-stat-val mono${accent ? ' accent' : ''}`}>{val}</div>
                <div className="sel-modal-stat-lbl">{lbl}</div>
              </div>
            ))}
          </div>
        </div>

        {/* ── Scrollable body ── */}
        <div className="sel-modal-body">
          {upcoming.length > 0 && (
            <div className="sel-modal-section">
              <div className="sel-modal-sec-title">
                <Icon name="cal" size={13} /> Próximos partidos
              </div>
              <div className="sel-match-mini-list">
                {upcoming.slice(0, 4).map((p) => (
                  <MatchMiniRow key={p.id} p={p} teamName={team.name} />
                ))}
              </div>
            </div>
          )}

          {finished.length > 0 && (
            <div className="sel-modal-section">
              <div className="sel-modal-sec-title">
                <Icon name="trophy" size={13} /> Resultados
              </div>
              <div className="sel-match-mini-list">
                {finished.map((p) => (
                  <MatchMiniRow key={p.id} p={p} teamName={team.name} />
                ))}
              </div>
            </div>
          )}

          {upcoming.length === 0 && finished.length === 0 && (
            <div className="sel-modal-no-data">
              <Icon name="cal" size={24} />
              <div>Partidos próximamente</div>
            </div>
          )}

          <div className="sel-modal-section">
            <div className="sel-modal-sec-title">
              <Icon name="star" size={13} /> Jugadores destacados
            </div>
            {playersLoading ? (
              <div className="sel-players-skel-grid">
                {Array.from({ length: 8 }).map((_, i) => (
                  <div key={i} className="sel-player-skel" />
                ))}
              </div>
            ) : players.length > 0 ? (
              <div className="sel-players-grid">
                {players.slice(0, 12).map((pl, i) => (
                  <div key={pl.id || i} className="sel-player-chip">
                    <div
                      className="sel-player-num mono"
                      style={{ background: cc.bg, color: cc.color }}
                    >
                      {pl.numero || pl.id || '#'}
                    </div>
                    <div className="sel-player-info">
                      <div className="sel-player-name">
                        {pl.nombre || pl.jugador || pl.nombreJugador || 'Jugador'}
                      </div>
                      {(pl.posicion || pl.position) && (
                        <div className="sel-player-pos">{pl.posicion || pl.position}</div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="sel-modal-no-data">
                <Icon name="star" size={24} />
                <div>Jugadores próximamente</div>
                <div className="sel-modal-no-data-sub">
                  Se actualizará con los datos del torneo
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

// ======= MAIN COMPONENT =======
const Selecciones = () => {
  const { dark, toggleDark } = useDarkMode();
  const {
    loggedIn, user, modal,
    openLogin, openRegister, closeModal,
    handleLoginSuccess, handleRegisterSuccess, handleLogout,
  } = useAuth();

  const [rawPartidos, setRawPartidos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(false);
  const [search, setSearch] = useState('');
  const [confFilter, setConfFilter] = useState('');
  const [selectedTeam, setSelectedTeam] = useState(null);

  const doLoad = () => {
    setLoading(true);
    setLoadError(false);
    getPartidosParaEquipos()
      .then((res) => { const d = res?.data; if (Array.isArray(d)) setRawPartidos(d); })
      .catch(() => setLoadError(true))
      .finally(() => setLoading(false));
  };
  useEffect(() => { doLoad(); }, []);

  const equipos = useMemo(() => deriveTeams(rawPartidos), [rawPartidos]);

  const availableConfs = useMemo(() => {
    const seen = new Set(equipos.map((t) => t.conf).filter((c) => c && c !== 'OTHER'));
    return CONF_ORDER.filter((c) => seen.has(c));
  }, [equipos]);

  const filtered = useMemo(() => {
    let out = equipos;
    if (confFilter) out = out.filter((t) => t.conf === confFilter);
    if (search.trim()) {
      const q = search.trim().toLowerCase();
      out = out.filter((t) => t.name.toLowerCase().includes(q));
    }
    return out;
  }, [equipos, confFilter, search]);

  return (
    <div className="app">
      <Navbar
        dark={dark} onToggleDark={toggleDark}
        loggedIn={loggedIn} user={user}
        onLogin={openLogin} onRegister={openRegister} onLogout={handleLogout}
      />

      <PageHero
        badge="Selecciones Clasificadas · FIFA World Cup 2026™"
        badgeIcon="shield"
        title={<>48 Selecciones<br />Una Copa del Mundo</>}
        subtitle="Explora los equipos clasificados, sus grupos, estadísticas y jugadores destacados del torneo más grande de la historia del fútbol."
        stats={[
          { num: '48', lbl: 'Selecciones'    },
          { num: '12', lbl: 'Grupos'         },
          { num: '6',  lbl: 'Confederaciones'},
          { num: '2026', lbl: 'Año'          },
        ]}
        variant="selecciones"
        mediaKey="selecciones"
      />

      {/* ── Main body ── */}
      <div className="container">
        <div className="sel-page-body">

          {/* Toolbar: search + confederation pills */}
          <div className="sel-toolbar">
            <div className="sel-search-box">
              <Icon name="search" size={15} />
              <input
                className="sel-search-input"
                type="text"
                placeholder="Buscar selección…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
              {search && (
                <button className="sel-search-clear" onClick={() => setSearch('')}>
                  <Icon name="close" size={12} />
                </button>
              )}
            </div>
            <div className="sel-conf-pills">
              <button
                className={'sel-conf-pill' + (!confFilter ? ' active' : '')}
                onClick={() => setConfFilter('')}
              >
                Todas
              </button>
              {availableConfs.map((c) => {
                const cc = confStyle(c);
                const isActive = confFilter === c;
                return (
                  <button
                    key={c}
                    className={'sel-conf-pill' + (isActive ? ' active' : '')}
                    style={isActive ? { background: cc.pill, color: '#fff', borderColor: cc.pill } : {}}
                    onClick={() => setConfFilter(isActive ? '' : c)}
                  >
                    {c}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Count bar */}
          <div className="sel-count-bar">
            <span className="sel-count-num mono">{loading ? '—' : filtered.length}</span>
            <span className="sel-count-lbl">
              {' '}selección{filtered.length !== 1 ? 'es' : ''}
            </span>
            {(search || confFilter) && !loading && filtered.length < equipos.length && (
              <span className="sel-count-note"> · {equipos.length - filtered.length} ocultas</span>
            )}
            {confFilter && (
              <span
                className="sel-count-conf"
                style={{ background: confStyle(confFilter).bg, color: confStyle(confFilter).color }}
              >
                {confFilter}
              </span>
            )}
          </div>

          {/* States: loading / error / empty / grid */}
          {loading ? (
            <div className="sel-grid">
              {Array.from({ length: 12 }).map((_, i) => <TeamCardSkeleton key={i} />)}
            </div>
          ) : loadError ? (
            <div className="sel-feedback-state">
              <div className="sel-feedback-icon" style={{ color: 'var(--live)' }}>
                <Icon name="x-circle" size={36} />
              </div>
              <div className="sel-feedback-title">No se pudieron cargar las selecciones</div>
              <div className="sel-feedback-sub">Verifica que el backend esté activo en el puerto 8080.</div>
              <button className="btn btn-ghost" style={{ marginTop: 12 }} onClick={doLoad}>
                <Icon name="refresh" size={14} /> Reintentar
              </button>
            </div>
          ) : filtered.length === 0 ? (
            <div className="sel-feedback-state">
              <div className="sel-feedback-icon">
                <Icon name="search" size={36} />
              </div>
              <div className="sel-feedback-title">Sin resultados</div>
              <div className="sel-feedback-sub">
                No hay selecciones que coincidan con "<strong>{search}</strong>".
              </div>
              <button className="btn btn-ghost" style={{ marginTop: 12 }} onClick={() => { setSearch(''); setConfFilter(''); }}>
                Limpiar filtros
              </button>
            </div>
          ) : (
            <div className="sel-grid">
              {filtered.map((team, idx) => (
                <TeamCard key={team.name} team={team} idx={idx} onClick={setSelectedTeam} />
              ))}
            </div>
          )}
        </div>
      </div>

      {selectedTeam && (
        <TeamDetailModal
          team={selectedTeam}
          rawPartidos={rawPartidos}
          onClose={() => setSelectedTeam(null)}
        />
      )}
      {modal === 'login' && (
        <LoginModal onClose={closeModal} onLoginSuccess={handleLoginSuccess} onSwitchToRegister={openRegister} />
      )}
      {modal === 'register' && (
        <RegisterModal onClose={closeModal} onRegisterSuccess={handleRegisterSuccess} onSwitchToLogin={openLogin} />
      )}

      <Footer />
    </div>
  );
};

export default Selecciones;
