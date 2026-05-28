import React, { useState, useEffect, useMemo } from 'react';
import { getTodosPartidos } from '../services/gruposService';
import Navbar from '../components/layout/Navbar';
import Footer from '../components/layout/Footer';
import Icon from '../components/ui/Icon';
import LoginModal from '../components/auth/LoginModal';
import RegisterModal from '../components/auth/RegisterModal';
import useAuth from '../hooks/useAuth';
import useDarkMode from '../hooks/useDarkMode';
import { teamIso2 } from '../utils/countries';
import { formatDate } from '../utils/formatters';
import TeamFlag from '../components/TeamFlag';
import PageHero from '../components/common/PageHero';
import '../App.css';

// ── Compute group standings with W/D/L + form ──
const computarGrupos = (partidos) => {
  if (!Array.isArray(partidos) || !partidos.length) return [];
  const groups = {};
  const matchesByGroup = {};
  const GROUP_RE = /^GROUP_([A-L])$/;

  partidos.forEach((p) => {
    const m = GROUP_RE.exec(p.fase || '');
    if (!m) return;
    const letra = m[1];
    if (!groups[letra]) { groups[letra] = {}; matchesByGroup[letra] = []; }

    const addTeam = (name) => {
      if (name && !groups[letra][name]) {
        groups[letra][name] = { name, iso2: teamIso2(name), pj: 0, w: 0, d: 0, l: 0, gf: 0, gc: 0, pts: 0, form: [] };
      }
    };
    addTeam(p.equipoLocal);
    addTeam(p.equipoVisitante);
    matchesByGroup[letra].push(p);

    if (p.estado !== 'FINALIZADO') return;
    const gl = p.golesLocal ?? 0;
    const gv = p.golesVisitante ?? 0;
    const loc = p.equipoLocal ? groups[letra][p.equipoLocal] : null;
    const vis = p.equipoVisitante ? groups[letra][p.equipoVisitante] : null;

    if (loc) { loc.pj++; loc.gf += gl; loc.gc += gv; }
    if (vis) { vis.pj++; vis.gf += gv; vis.gc += gl; }

    if (gl > gv) {
      if (loc) { loc.w++; loc.pts += 3; loc.form.unshift('W'); }
      if (vis) { vis.l++; vis.form.unshift('L'); }
    } else if (gl === gv) {
      if (loc) { loc.d++; loc.pts++; loc.form.unshift('D'); }
      if (vis) { vis.d++; vis.pts++; vis.form.unshift('D'); }
    } else {
      if (vis) { vis.w++; vis.pts += 3; vis.form.unshift('W'); }
      if (loc) { loc.l++; loc.form.unshift('L'); }
    }
  });

  return Object.entries(groups)
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([letra, equipos]) => {
      const rows = Object.values(equipos)
        .sort((a, b) => b.pts - a.pts || (b.gf - b.gc) - (a.gf - a.gc) || b.gf - a.gf)
        .map((e, i) => ({
          team: { name: e.name, iso2: e.iso2 },
          pj: e.pj, w: e.w, d: e.d, l: e.l,
          gf: e.gf, gc: e.gc, gd: e.gf - e.gc, pts: e.pts,
          form: e.form.slice(0, 3),
          qualified: i < 2,
        }));
      const matches = (matchesByGroup[letra] || [])
        .sort((a, b) => new Date(a.fechaHora || 0) - new Date(b.fechaHora || 0));
      return { letra, rows, matches };
    });
};

// ── Compute knockout brackets ──
const computarEliminatoria = (partidos) => {
  if (!Array.isArray(partidos)) return {};
  const KO = ['LAST_32', 'LAST_16', 'QUARTER_FINALS', 'SEMI_FINALS', 'FINAL', 'THIRD_PLACE'];
  const result = {};
  KO.forEach((phase) => {
    result[phase] = partidos
      .filter((p) => p.fase === phase)
      .sort((a, b) => new Date(a.fechaHora || 0) - new Date(b.fechaHora || 0));
  });
  return result;
};

// ── Form chip (W / D / L) ──
const FormChip = ({ r }) => {
  const cfg = { W: ['form-chip w', 'V'], D: ['form-chip d', 'E'], L: ['form-chip l', 'D'] };
  const [cls, label] = cfg[r] || ['form-chip', '?'];
  return <span className={cls} title={{ W: 'Victoria', D: 'Empate', L: 'Derrota' }[r]}>{label}</span>;
};

// ── Group card skeleton ──
const GroupSkeleton = () => (
  <div className="pos-group-card pos-group-skel">
    <div className="pos-skel-head" />
    {[0, 1, 2, 3].map((i) => (
      <div key={i} className="pos-skel-row" style={{ animationDelay: `${i * 0.08}s` }} />
    ))}
  </div>
);

// ── Group standings card ──
const GroupCard = ({ group, savedMatches, onToggleSave }) => {
  const hasPlayed = group.rows.some((r) => r.pj > 0);
  return (
    <div className="pos-group-card">
      <div className="group-head">
        <span className="group-letter">Grupo {group.letra}</span>
        <span className="group-status">{hasPlayed ? 'En curso' : 'Por jugar'}</span>
      </div>

      <div className="pos-standings-wrap">
        <table className="pos-standings">
          <thead>
            <tr>
              <th className="col-rank">#</th>
              <th className="col-team">Equipo</th>
              <th title="Partidos jugados">PJ</th>
              <th title="Victorias">V</th>
              <th title="Empates">E</th>
              <th title="Derrotas">D</th>
              <th title="Goles a favor">GF</th>
              <th title="Goles en contra">GC</th>
              <th title="Diferencia de goles">DG</th>
              <th className="col-pts" title="Puntos">Pts</th>
              <th title="Últimos resultados">Forma</th>
            </tr>
          </thead>
          <tbody>
            {group.rows.map((row, i) => (
              <tr key={row.team.name} className={'pos-row' + (row.qualified ? ' qualified' : '')}>
                <td className="col-rank mono">{i + 1}</td>
                <td className="col-team">
                  <span className="pos-flag">
                    <TeamFlag iso2={row.team.iso2} size="row" />
                  </span>
                  <span className="pos-tname">{row.team.name}</span>
                </td>
                <td className="mono">{row.pj}</td>
                <td className="mono">{row.w}</td>
                <td className="mono">{row.d}</td>
                <td className="mono">{row.l}</td>
                <td className="mono">{row.gf}</td>
                <td className="mono">{row.gc}</td>
                <td className={`mono gd${row.gd > 0 ? ' pos' : row.gd < 0 ? ' neg' : ''}`}>
                  {row.gd > 0 ? `+${row.gd}` : row.gd}
                </td>
                <td className="col-pts mono">{row.pts}</td>
                <td>
                  <div className="form-chips">
                    {row.form.length > 0
                      ? row.form.map((r, j) => <FormChip key={j} r={r} />)
                      : <span style={{ color: 'var(--ink-4)', fontSize: 11 }}>—</span>}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {group.matches.length > 0 && (
        <div className="pos-matches-section">
          <div className="pos-matches-title">
            <Icon name="cal" size={11} /> Partidos del grupo
          </div>
          {group.matches.map((m, i) => {
            const mid = m.id || `${group.letra}-${i}`;
            const done = m.estado === 'FINALIZADO';
            const live = m.estado === 'EN_JUEGO';
            return (
              <div key={mid} className={'pos-match-item' + (live ? ' live' : done ? ' done' : '')}>
                <div className="pos-match-teams">
                  <span className="pos-match-team">
                    <TeamFlag iso2={teamIso2(m.equipoLocal)} size="row" />
                    <span>{m.equipoLocal || 'TBD'}</span>
                  </span>
                  <span className="pos-match-score">
                    {done || live
                      ? <strong>{m.golesLocal ?? 0}–{m.golesVisitante ?? 0}</strong>
                      : <span className="score-dash">vs</span>}
                  </span>
                  <span className="pos-match-team right">
                    <span>{m.equipoVisitante || 'TBD'}</span>
                    <TeamFlag iso2={teamIso2(m.equipoVisitante)} size="row" />
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

// ── Knockout bracket slot ──
const BracketSlot = ({ match }) => {
  const done = match?.estado === 'FINALIZADO';
  const homeWin = done && (match?.golesLocal ?? 0) > (match?.golesVisitante ?? 0);
  const awayWin = done && (match?.golesVisitante ?? 0) > (match?.golesLocal ?? 0);
  const hasData = match && (match.equipoLocal || match.equipoVisitante);

  return (
    <div className="bracket-slot">
      <div className={`bracket-team${homeWin ? ' winner' : ''}`}>
        {hasData && match.equipoLocal ? (
          <>
            <TeamFlag iso2={teamIso2(match.equipoLocal)} size="row" />
            <span className="bracket-tname">{match.equipoLocal}</span>
            {done && <span className="bracket-score mono">{match.golesLocal ?? 0}</span>}
          </>
        ) : <span className="bracket-tbd">TBD</span>}
      </div>
      <div className={`bracket-team${awayWin ? ' winner' : ''}`}>
        {hasData && match.equipoVisitante ? (
          <>
            <TeamFlag iso2={teamIso2(match.equipoVisitante)} size="row" />
            <span className="bracket-tname">{match.equipoVisitante}</span>
            {done && <span className="bracket-score mono">{match.golesVisitante ?? 0}</span>}
          </>
        ) : <span className="bracket-tbd">TBD</span>}
      </div>
      {match?.fechaHora && (
        <div className="bracket-date mono">{formatDate(match.fechaHora)}</div>
      )}
    </div>
  );
};

const BracketRound = ({ title, matches, slots }) => (
  <div className="bracket-round">
    <div className="bracket-round-title">{title}</div>
    <div className="bracket-round-slots">
      {Array.from({ length: slots }, (_, i) => (
        <BracketSlot key={i} match={matches[i] || null} />
      ))}
    </div>
  </div>
);

// ── Group Stage tab ──
const GroupStageTab = ({ grupos, loading, error, onRetry }) => {
  const [saved, setSaved] = useState(() => {
    try { return new Set(JSON.parse(localStorage.getItem('pos_saved') || '[]')); } catch { return new Set(); }
  });
  const toggleSave = (mid) => {
    setSaved((prev) => {
      const next = new Set(prev);
      if (next.has(mid)) next.delete(mid); else next.add(mid);
      localStorage.setItem('pos_saved', JSON.stringify([...next]));
      return next;
    });
  };

  if (loading) return (
    <div className="pos-groups-grid">
      {Array.from({ length: 12 }).map((_, i) => <GroupSkeleton key={i} />)}
    </div>
  );

  if (error) return (
    <div className="pos-feedback">
      <div style={{ color: 'var(--live)' }}><Icon name="x-circle" size={36} /></div>
      <div className="pos-feedback-title">Error al cargar los datos</div>
      <div className="pos-feedback-sub">Verifica que el backend esté activo en el puerto 8080.</div>
      <button className="btn btn-ghost" style={{ marginTop: 12 }} onClick={onRetry}>
        <Icon name="refresh" size={14} /> Reintentar
      </button>
    </div>
  );

  if (grupos.length === 0) return (
    <div className="pos-feedback">
      <Icon name="shield" size={36} />
      <div className="pos-feedback-title">Sin datos de grupos</div>
      <div className="pos-feedback-sub">Sincroniza los partidos desde el Calendario para ver las posiciones.</div>
    </div>
  );

  return (
    <div className="pos-groups-grid">
      {grupos.map((g) => (
        <GroupCard key={g.letra} group={g} savedMatches={saved} onToggleSave={toggleSave} />
      ))}
    </div>
  );
};

// ── Knockout tab ──
const KnockoutTab = ({ koMatches }) => {
  const r32 = koMatches.LAST_32 || [];
  const r16 = koMatches.LAST_16 || [];
  const qf  = koMatches.QUARTER_FINALS || [];
  const sf  = koMatches.SEMI_FINALS || [];
  const fin = koMatches.FINAL || [];
  const t3  = koMatches.THIRD_PLACE || [];
  const hasAny = r32.length || r16.length || qf.length || sf.length || fin.length;

  if (!hasAny) return (
    <div className="pos-feedback">
      <Icon name="bracket" size={36} />
      <div className="pos-feedback-title">Fase eliminatoria próximamente</div>
      <div className="pos-feedback-sub">Los brackets se activarán una vez termine la fase de grupos.</div>
    </div>
  );

  return (
    <div className="bracket-wrapper">
      <p className="bracket-note">
        Los brackets se actualizan automáticamente conforme avanza el torneo. Equipos sin datos se muestran como TBD.
      </p>
      <div className="bracket-scroll">
        <div className="bracket-tree">
          {r32.length > 0  && <BracketRound title="32avos" matches={r32} slots={16} />}
          {r16.length > 0  && <BracketRound title="Octavos" matches={r16} slots={8} />}
          {qf.length  > 0  && <BracketRound title="Cuartos" matches={qf}  slots={4} />}
          {sf.length  > 0  && <BracketRound title="Semifinal" matches={sf} slots={2} />}
          <div className="bracket-round bracket-final-col">
            <div className="bracket-round-title">Final</div>
            <div className="bracket-round-slots">
              <BracketSlot match={fin[0] || null} />
            </div>
            {t3.length > 0 && (
              <>
                <div className="bracket-round-title" style={{ marginTop: 28 }}>3er Lugar</div>
                <div className="bracket-round-slots">
                  <BracketSlot match={t3[0]} />
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

// ── Main page ──
const Posiciones = () => {
  const { dark, toggleDark } = useDarkMode();
  const {
    loggedIn, user, modal,
    openLogin, openRegister, closeModal,
    handleLoginSuccess, handleRegisterSuccess, handleLogout,
  } = useAuth();

  const [rawPartidos, setRawPartidos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [tab, setTab] = useState('groups');

  const doLoad = () => {
    setLoading(true); setError(false);
    getTodosPartidos()
      .then((res) => { const d = res?.data; if (Array.isArray(d)) setRawPartidos(d); })
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  };
  useEffect(() => { doLoad(); }, []);

  const grupos    = useMemo(() => computarGrupos(rawPartidos), [rawPartidos]);
  const koMatches = useMemo(() => computarEliminatoria(rawPartidos), [rawPartidos]);

  const totalEquipos = useMemo(() => {
    const names = new Set();
    rawPartidos.forEach((p) => { if (p.equipoLocal) names.add(p.equipoLocal); if (p.equipoVisitante) names.add(p.equipoVisitante); });
    return names.size;
  }, [rawPartidos]);

  const partidosJugados = useMemo(
    () => rawPartidos.filter((p) => p.estado === 'FINALIZADO').length,
    [rawPartidos]
  );

  const TABS = [
    { id: 'groups',   label: 'Fase de Grupos',      icon: 'shield' },
    { id: 'knockout', label: 'Eliminación Directa',  icon: 'bracket' },
  ];

  return (
    <div className="app">
      <Navbar
        dark={dark} onToggleDark={toggleDark}
        loggedIn={loggedIn} user={user}
        onLogin={openLogin} onRegister={openRegister} onLogout={handleLogout}
      />

      <PageHero
        badge="Tabla de Posiciones · FIFA World Cup 2026™"
        badgeIcon="trophy"
        title={<>Posiciones<br />y Brackets</>}
        subtitle="Clasificación en tiempo real de los 12 grupos y el cuadro de eliminación directa del Mundial 2026."
        stats={[
          { num: loading ? '—' : grupos.length,       lbl: 'Grupos'  },
          { num: loading ? '—' : totalEquipos,        lbl: 'Equipos' },
          { num: loading ? '—' : partidosJugados,     lbl: 'Jugados' },
          { num: '2026',                               lbl: 'Año'     },
        ]}
        variant="posiciones"
        mediaKey="posiciones"
      />

      {/* ── Tabs ── */}
      <div className="pos-tabs-section">
        <div className="container">
          <div className="pos-tabs-pills">
            {TABS.map(({ id, label, icon }) => (
              <button
                key={id}
                className={'pos-tab-pill' + (tab === id ? ' active' : '')}
                onClick={() => setTab(id)}
              >
                <Icon name={icon} size={14} />
                {label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* ── Content ── */}
      <div className="container pos-content">
        {tab === 'groups' && (
          <GroupStageTab grupos={grupos} loading={loading} error={error} onRetry={doLoad} />
        )}
        {tab === 'knockout' && (
          <KnockoutTab koMatches={koMatches} />
        )}
      </div>

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

export default Posiciones;
