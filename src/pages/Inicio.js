import React, { useState, useEffect, useRef } from 'react';
import { getProximosPartidos, getResultados, getEnJuego, agregarAgenda } from '../services/partidoService';
import { getTodosPartidos } from '../services/gruposService';
import { getAlbumUsuario } from '../services/albumService';
import TeamFlag from '../components/TeamFlag';
import Navbar from '../components/layout/Navbar';
import Footer from '../components/layout/Footer';
import Icon from '../components/ui/Icon';
import MatchModal from '../components/ui/MatchModal';
import LoginModal from '../components/auth/LoginModal';
import RegisterModal from '../components/auth/RegisterModal';
import HeroMedia from '../components/common/HeroMedia';
import LegendsShowcase from '../components/common/LegendsShowcase';
import { getHeroMedia } from '../components/common/pageHeroMedia';
import useAuth from '../hooks/useAuth';
import useDarkMode from '../hooks/useDarkMode';
import { teamIso2 } from '../utils/countries';
import { adaptPhase } from '../utils/phases';
import { formatDate, formatTime } from '../utils/formatters';
import '../App.css';

// ======= STATIC DATA =======
const TEAMS = {
  MEX: { code: 'MEX', name: 'México',         iso2: 'MX' },
  CAN: { code: 'CAN', name: 'Canadá',          iso2: 'CA' },
  USA: { code: 'USA', name: 'Estados Unidos',  iso2: 'US' },
  ARG: { code: 'ARG', name: 'Argentina',       iso2: 'AR' },
  BRA: { code: 'BRA', name: 'Brasil',          iso2: 'BR' },
  FRA: { code: 'FRA', name: 'Francia',         iso2: 'FR' },
  ESP: { code: 'ESP', name: 'España',          iso2: 'ES' },
  GER: { code: 'GER', name: 'Alemania',        iso2: 'DE' },
  POR: { code: 'POR', name: 'Portugal',        iso2: 'PT' },
  ENG: { code: 'ENG', name: 'Inglaterra',      iso2: 'GB' },
  NED: { code: 'NED', name: 'Países Bajos',    iso2: 'NL' },
  CRO: { code: 'CRO', name: 'Croacia',         iso2: 'HR' },
  URU: { code: 'URU', name: 'Uruguay',         iso2: 'UY' },
  COL: { code: 'COL', name: 'Colombia',        iso2: 'CO' },
  JPN: { code: 'JPN', name: 'Japón',           iso2: 'JP' },
  BEL: { code: 'BEL', name: 'Bélgica',        iso2: 'BE' },
};

const MOCK_RESULTS = [
  { id: 10, home: TEAMS.ARG, away: TEAMS.FRA, hs: 2, as: 1, phase: 'Octavos de Final', date: 'Hoy', live: true, liveLabel: "67'" },
  { id: 11, home: TEAMS.BRA, away: TEAMS.CRO, hs: 3, as: 0, phase: 'Grupo F', date: '25 Jun' },
  { id: 12, home: TEAMS.ESP, away: TEAMS.JPN, hs: 2, as: 2, phase: 'Grupo E', date: '25 Jun' },
  { id: 13, home: TEAMS.GER, away: TEAMS.POR, hs: 1, as: 1, phase: 'Grupo D', date: '24 Jun' },
  { id: 14, home: TEAMS.MEX, away: TEAMS.USA, hs: 1, as: 0, phase: 'Grupo A', date: '24 Jun' },
];

// ======= BACKEND DATA ADAPTERS =======
const adaptUpcoming = (p) => ({
  id: p.id,
  home: { name: p.equipoLocal || '?', iso2: teamIso2(p.equipoLocal) },
  away: { name: p.equipoVisitante || '?', iso2: teamIso2(p.equipoVisitante) },
  phase: adaptPhase(p.fase),
  venue: p.sede?.nombreEstadio || 'Por confirmar',
  city: p.sede?.ciudad || '',
  date: formatDate(p.fechaHora),
  time: formatTime(p.fechaHora),
});

const adaptResult = (p) => ({
  id: p.id,
  home: { name: p.equipoLocal || '?', iso2: teamIso2(p.equipoLocal) },
  away: { name: p.equipoVisitante || '?', iso2: teamIso2(p.equipoVisitante) },
  hs: p.golesLocal ?? 0,
  as: p.golesVisitante ?? 0,
  phase: adaptPhase(p.fase),
  date: formatDate(p.fechaHora),
  live: p.estado === 'EN_JUEGO',
  liveLabel: '',
});

const adaptLive = (p) => ({
  home: { name: p.equipoLocal || '?', iso2: teamIso2(p.equipoLocal) },
  away: { name: p.equipoVisitante || '?', iso2: teamIso2(p.equipoVisitante) },
  homeScore: p.golesLocal ?? 0,
  awayScore: p.golesVisitante ?? 0,
  minute: 0,
  phase: adaptPhase(p.fase),
  venue: p.sede?.nombreEstadio || '',
  date: formatDate(p.fechaHora),
  events: [],
});

// Deriva tabla de grupos desde /partido/getAll
const computarGrupos = (partidos) => {
  if (!Array.isArray(partidos) || partidos.length === 0) return [];
  const groups = {};
  const GROUP_RE = /^GROUP_([A-L])$/;

  partidos.forEach((p) => {
    const m = GROUP_RE.exec(p.fase || '');
    if (!m) return;
    const letra = m[1];
    if (!groups[letra]) groups[letra] = {};

    const addTeam = (name) => {
      if (name && !groups[letra][name]) {
        groups[letra][name] = { name, iso2: teamIso2(name), pj: 0, gf: 0, gc: 0, pts: 0 };
      }
    };
    addTeam(p.equipoLocal);
    addTeam(p.equipoVisitante);

    if (p.estado !== 'FINALIZADO') return;
    const gl = p.golesLocal ?? 0;
    const gv = p.golesVisitante ?? 0;
    if (p.equipoLocal) {
      groups[letra][p.equipoLocal].pj += 1;
      groups[letra][p.equipoLocal].gf += gl;
      groups[letra][p.equipoLocal].gc += gv;
    }
    if (p.equipoVisitante) {
      groups[letra][p.equipoVisitante].pj += 1;
      groups[letra][p.equipoVisitante].gf += gv;
      groups[letra][p.equipoVisitante].gc += gl;
    }
    if (gl > gv && p.equipoLocal)       groups[letra][p.equipoLocal].pts += 3;
    else if (gl === gv) {
      if (p.equipoLocal)      groups[letra][p.equipoLocal].pts += 1;
      if (p.equipoVisitante)  groups[letra][p.equipoVisitante].pts += 1;
    } else if (p.equipoVisitante)        groups[letra][p.equipoVisitante].pts += 3;
  });

  return Object.entries(groups)
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([letra, equipos]) => {
      const rows = Object.values(equipos)
        .sort((a, b) => b.pts - a.pts || (b.gf - b.gc) - (a.gf - a.gc) || b.gf - a.gf)
        .map((e, i) => ({
          team: { code: e.name.slice(0, 3).toUpperCase(), name: e.name, iso2: e.iso2 },
          pj: e.pj, gf: e.gf, gc: e.gc, pts: e.pts,
          qualified: i < 2,
        }));
      return { letra, status: 'En curso', rows };
    });
};

// Para el endpoint dedicado /grupo/tabla cuando el backend lo implemente.
// eslint-disable-next-line no-unused-vars
const adaptGrupos = (data) => {
  if (!Array.isArray(data) || data.length === 0) return [];
  return data.map((g) => ({
    letra: g.letra || g.grupo || '?',
    status: g.estado || 'En curso',
    rows: (g.equipos || []).map((e) => ({
      team: {
        code: e.codigo || e.nombre?.slice(0, 3).toUpperCase(),
        name: e.nombre || '?',
        iso2: teamIso2(e.nombre),
      },
      pj: e.pj ?? 0,
      gf: e.gf ?? 0,
      gc: e.gc ?? 0,
      pts: e.pts ?? 0,
      qualified: e.clasificado ?? false,
    })),
  }));
};

// ======= HORIZONTAL SCROLL HOOK =======
const useHorizontalScroll = () => {
  const ref = useRef(null);
  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    let target = 0, current = 0, rafId = null;
    const lerp = (a, b, t) => a + (b - a) * t;
    const animate = () => {
      current = lerp(current, target, 0.1);
      el.scrollLeft = current;
      if (Math.abs(target - current) > 0.5) {
        rafId = requestAnimationFrame(animate);
      } else {
        el.scrollLeft = target;
        rafId = null;
      }
    };
    const onWheel = (e) => {
      if (Math.abs(e.deltaY) > Math.abs(e.deltaX)) {
        e.preventDefault();
        if (rafId === null) current = el.scrollLeft;
        target = Math.max(0, Math.min(el.scrollWidth - el.clientWidth, target + e.deltaY * 1.5));
        if (rafId === null) rafId = requestAnimationFrame(animate);
      }
    };
    el.addEventListener('wheel', onWheel, { passive: false });
    return () => { el.removeEventListener('wheel', onWheel); if (rafId) cancelAnimationFrame(rafId); };
  }, []);
  return ref;
};

const HScrollRow = ({ children }) => {
  const ref = useHorizontalScroll();
  return <div className="scroll-row" ref={ref}>{children}</div>;
};

// ======= HERO =======
const WC_START = new Date('2026-06-11T23:00:00Z');

const calcCountdown = () => {
  const diff = Math.max(0, WC_START - new Date());
  return {
    d: Math.floor(diff / 86400000),
    h: Math.floor((diff % 86400000) / 3600000),
    m: Math.floor((diff % 3600000) / 60000),
    s: Math.floor((diff % 60000) / 1000),
  };
};

const Hero = ({ liveMatch, nextMatch }) => {
  const wcStarted = new Date() >= WC_START;
  const [mode, setMode] = useState(!wcStarted ? 'countdown' : (liveMatch ? 'live' : 'upcoming'));
  const [minute, setMinute] = useState(liveMatch?.minute ?? 0);
  const [cd, setCd] = useState(calcCountdown);

  useEffect(() => {
    if (!wcStarted) { setMode('countdown'); return; }
    setMode(liveMatch ? 'live' : 'upcoming');
    if (liveMatch) setMinute(liveMatch.minute);
  }, [liveMatch, wcStarted]);

  useEffect(() => {
    if (mode !== 'live') return;
    const i = setInterval(() => setMinute((m) => m < 90 ? m + 1 : 90), 8000);
    return () => clearInterval(i);
  }, [mode]);

  useEffect(() => {
    const i = setInterval(() => setCd(calcCountdown()), 1000);
    return () => clearInterval(i);
  }, []);

  if (mode === 'countdown') {
    return (
      <section className="hero-epic">
        <div className="hero-bg"></div>
        <HeroMedia media={getHeroMedia('inicio')} intensity="default" />
        <div className="hero-countdown">
            <div className="hero-epic-eyebrow">
              <span className="hero-epic-chip">
                <Icon name="trophy" size={11} /> FIFA World Cup 2026™
              </span>
            </div>
            <div className="hero-cd-title">LA FIESTA EMPIEZA EN</div>
            <div className="hero-cd-row">
              {[['d', 'Días'], ['h', 'Horas'], ['m', 'Min'], ['s', 'Seg']].map(([k, l], idx, arr) => (
                <React.Fragment key={k}>
                  <div className="hero-cd-block">
                    <div className="hero-cd-num">{String(cd[k]).padStart(2, '0')}</div>
                    <div className="hero-cd-label">{l}</div>
                  </div>
                  {idx < arr.length - 1 && <div className="hero-cd-sep">:</div>}
                </React.Fragment>
              ))}
            </div>
            <div className="hero-cd-info">
              <span><Icon name="cal" size={14} /> 11 de junio, 2026</span>
              <span className="sep">·</span>
              <span>Primera jornada · Grupo A</span>
              <span className="sep">·</span>
              <span><Icon name="pin" size={14} /> 16 sedes en USA, MEX y CAN</span>
            </div>
            <div className="hero-cta-row hero-cd-cta">
              <button className="hero-cta primary" onClick={() => document.getElementById('proximos')?.scrollIntoView({ behavior: 'smooth' })}>
                <Icon name="cal" size={14} /> Ver partidos
              </button>
              <button className="hero-cta ghost" onClick={() => document.getElementById('grupos')?.scrollIntoView({ behavior: 'smooth' })}>
                <Icon name="users" size={14} /> Ver grupos
              </button>
            </div>
        </div>
      </section>
    );
  }

  const isLive = mode === 'live';
  const match = isLive ? liveMatch : nextMatch;
  if (!match) return null;
  const scores = isLive ? [match.homeScore, match.awayScore] : [null, null];
  const events = isLive ? (match.events || []) : [];

  return (
    <section className="hero-epic">
      <HeroMedia media={getHeroMedia('inicio')} intensity="strong" />
      <div className="hero-card">
        <div className="hero-bg"></div>
        <div className="hero-left">
          <div>
            {isLive ? (
              <div className="hero-badge live">
                <span className="live-dot"></span>
                EN VIVO · {minute}'
              </div>
            ) : (
              <div className="hero-badge">
                <Icon name="cal" size={12} /> PRÓXIMO PARTIDO
              </div>
            )}
            <div className="hero-meta">
              {match.phase}
              <span className="sep">·</span>
              {match.venue}
              <span className="sep">·</span>
              {match.date}
            </div>
          </div>

          <div className="hero-match">
            <div className="hero-match-row">
              <div className="hero-team">
                <div className="hero-flag"><TeamFlag iso2={match.home.iso2} size="hero" /></div>
                <div className="hero-team-name">{match.home.name}</div>
              </div>
              {isLive ? (
                <div className="hero-score">
                  <span>{scores[0]}</span>
                  <span className="dash">—</span>
                  <span>{scores[1]}</span>
                </div>
              ) : (
                <div className="hero-score">
                  <span className="dash" style={{ fontSize: 44 }}>VS</span>
                </div>
              )}
              <div className="hero-team right">
                <div className="hero-flag"><TeamFlag iso2={match.away.iso2} size="hero" /></div>
                <div className="hero-team-name" style={{ textAlign: 'right' }}>{match.away.name}</div>
              </div>
            </div>
            {isLive && (
              <div className="hero-minute">
                <span className="min-dot">●</span> 2T · {minute}' en juego
              </div>
            )}
          </div>

          <div className="hero-cta-row">
            <button className="hero-cta primary">
              <Icon name="play" size={14} /> {isLive ? 'Ver en vivo' : 'Ver detalle'}
            </button>
            <button className="hero-cta ghost">
              <Icon name="ticket" size={14} /> {isLive ? 'Resumen' : 'Comprar entrada'}
            </button>
            {liveMatch && (
              <button className="hero-cta ghost" onClick={() => setMode(isLive ? 'upcoming' : 'live')}>
                {isLive ? 'Próximo partido' : 'Ver en vivo'}
              </button>
            )}
          </div>
        </div>

        <div className="hero-right">
          {isLive ? (
            <>
              <div className="hero-right-title">Momentos clave</div>
              <div className="timeline">
                {events.map((e, i) => (
                  <div className="event" key={i} style={{ animationDelay: `${i * 0.08}s` }}>
                    <span className="event-min">{e.min}</span>
                    <span className="event-icon">{e.icon}</span>
                    <span className="event-text">{e.text}</span>
                  </div>
                ))}
              </div>
            </>
          ) : (
            <>
              <div className="hero-right-title">Empieza en</div>
              <div className="countdown">
                {[['d', 'Días'], ['h', 'Horas'], ['m', 'Min'], ['s', 'Seg']].map(([k, l]) => (
                  <div className="countdown-box" key={k}>
                    <div className="countdown-num">{String(cd[k]).padStart(2, '0')}</div>
                    <div className="countdown-label">{l}</div>
                  </div>
                ))}
              </div>
              <div style={{ marginTop: 28 }}>
                <div className="hero-right-title">Quiniela de este partido</div>
                <div style={{ background: 'rgba(255,255,255,.04)', border: '1px solid rgba(255,255,255,.07)', borderRadius: 14, padding: 16 }}>
                  <div style={{ fontSize: 12, color: 'rgba(255,255,255,.5)', marginBottom: 8 }}>Probabilidad comunidad</div>
                  <div style={{ display: 'flex', height: 8, borderRadius: 999, overflow: 'hidden' }}>
                    <div style={{ width: '48%', background: 'var(--accent)' }}></div>
                    <div style={{ width: '24%', background: 'rgba(255,255,255,.15)' }}></div>
                    <div style={{ width: '28%', background: 'rgba(255,255,255,.3)' }}></div>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, marginTop: 10, fontFamily: 'var(--font-mono)' }}>
                    <span>{match.home.name} 48%</span>
                    <span style={{ color: 'rgba(255,255,255,.5)' }}>X 24%</span>
                    <span>{match.away.name} 28%</span>
                  </div>
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </section>
  );
};

// ======= QUICK ACTIONS =======
const QuickActions = ({ loggedIn, onToast, onLogin }) => (
  <section className="section container">
    <div className="section-head">
      <div>
        <h2 className="section-title">Accesos rápidos</h2>
        <div className="section-sub">Todo lo que importa, a un tap</div>
      </div>
    </div>
    <div className="quick-grid">
      <button className="quick feature" onClick={() => window.location.href = '/album'}>
        <div className="quick-icon"><Icon name="sparkles" size={20} /></div>
        <div>
          <div className="quick-title" style={{ fontSize: 20 }}>Abrir sobre de cromos</div>
          <div className="quick-sub">{loggedIn ? '2 sobres disponibles' : 'Regístrate para empezar tu álbum'}</div>
        </div>
        <div className="quick-arrow"><Icon name="arrow-ur" size={18} /></div>
      </button>

      <button className="quick" onClick={() => window.location.href = '/calendario'}>
        <div className="quick-icon"><Icon name="cal" size={18} /></div>
        <div>
          <div className="quick-title">Calendario</div>
          <div className="quick-sub">104 partidos</div>
        </div>
        <div className="quick-arrow"><Icon name="arrow-ur" size={16} /></div>
      </button>

      <button className="quick" onClick={() => window.location.href = '/entradas'}>
        <div className="quick-icon"><Icon name="ticket" size={18} /></div>
        <div>
          <div className="quick-title">Comprar entradas</div>
          <div className="quick-sub">Octavos desde $180</div>
        </div>
        <div className="quick-arrow"><Icon name="arrow-ur" size={16} /></div>
      </button>

      <button className="quick" onClick={() => { window.location.href = '/sedes'; }}>
        <div className="quick-icon"><Icon name="stadium" size={18} /></div>
        <div>
          <div className="quick-title">16 sedes</div>
          <div className="quick-sub">USA · MEX · CAN</div>
        </div>
        <div className="quick-arrow"><Icon name="arrow-ur" size={16} /></div>
      </button>

      <button className="quick" onClick={() => { window.location.href = '/pollas'; }}>
        <div className="quick-icon"><Icon name="trophy" size={18} /></div>
        <div>
          <div className="quick-title">Crear polla</div>
          <div className="quick-sub">Invita amigos</div>
        </div>
        <div className="quick-arrow"><Icon name="arrow-ur" size={16} /></div>
      </button>

      <button className="quick" onClick={() => (window.location.href = '/selecciones')}>
        <div className="quick-icon"><Icon name="users" size={18} /></div>
        <div>
          <div className="quick-title">Selecciones</div>
          <div className="quick-sub">48 equipos, grupos y jugadores</div>
        </div>
        <div className="quick-arrow"><Icon name="arrow-ur" size={16} /></div>
      </button>
    </div>
  </section>
);

// ======= MATCH CARDS =======
const MatchCard = ({ m, onMoreInfo }) => (
  <div className="match-card" onClick={onMoreInfo} style={{ cursor: 'pointer' }}>
    <div className="match-top">
      <span className="match-phase">{m.phase}</span>
      <span className="match-date mono">
        {m.date}{m.date && m.time ? ' · ' : ''}{m.time}
      </span>
    </div>
    <div className="match-body">
      <div className="match-team-row">
        <div className="match-team">
          <div className="team-flag"><TeamFlag iso2={m.home.iso2} size="card" /></div>
          <div className="team-name">{m.home.name}</div>
        </div>
        <div className="team-score muted">—</div>
      </div>
      <div className="match-team-row">
        <div className="match-team">
          <div className="team-flag"><TeamFlag iso2={m.away.iso2} size="card" /></div>
          <div className="team-name">{m.away.name}</div>
        </div>
        <div className="team-score muted">—</div>
      </div>
    </div>
    <div className="match-footer">
      <span className="match-venue">
        <Icon name="pin" size={13} /> {m.venue || 'Por confirmar'}
      </span>
      <span className="match-cta">Más información <Icon name="arrow" size={12} /></span>
    </div>
  </div>
);

const ResultCard = ({ r }) => {
  const homeWin = r.hs > r.as;
  const awayWin = r.as > r.hs;
  return (
    <div className={'match-card result-card' + (r.live ? ' live' : '')}>
      <div className="match-top">
        <span className="match-phase">
          {r.live && <span className="live-pulse"></span>}
          {r.live ? `EN VIVO · ${r.liveLabel}` : r.phase}
        </span>
        <span className="match-date mono">{r.date}</span>
      </div>
      <div className="match-body">
        <div className="match-team-row">
          <div className="match-team">
            <div className="team-flag"><TeamFlag iso2={r.home.iso2} size="card" /></div>
            <div className="team-name">{r.home.name}</div>
          </div>
          <div className={'team-score' + (awayWin ? ' loser' : '')}>{r.hs}</div>
        </div>
        <div className="match-team-row">
          <div className="match-team">
            <div className="team-flag"><TeamFlag iso2={r.away.iso2} size="card" /></div>
            <div className="team-name">{r.away.name}</div>
          </div>
          <div className={'team-score' + (homeWin ? ' loser' : '')}>{r.as}</div>
        </div>
      </div>
      <div className="match-footer">
        <span className="match-venue">{r.live ? 'Ver en vivo' : 'Resumen y goles'}</span>
        <span className="match-cta"><Icon name="arrow" size={12} /></span>
      </div>
    </div>
  );
};

// ======= ALBUM PANEL =======
const AlbumPanel = ({ loggedIn, onLogin, onToast, albumData }) => {
  const { laminasObtenidas = 0, totalLaminas = 0, porcentaje = 0 } = albumData || {};
  const R = 52, C = 2 * Math.PI * R;
  const offset = loggedIn ? C - porcentaje / 100 * C : C;
  return (
    <div className="panel" id="album">
      <div className="panel-head">
        <div>
          <div className="panel-title">Tu álbum</div>
          <div className="panel-sub">{loggedIn ? 'Sigue coleccionando' : 'Empieza tu colección'}</div>
        </div>
        <a className="section-link" href="/album">Ver álbum <Icon name="arrow" size={12} /></a>
      </div>
      <div className="album-visual">
        <div className="album-ring">
          <svg width="120" height="120">
            <circle cx="60" cy="60" r={R} fill="none" strokeWidth="8" className="track" />
            <circle cx="60" cy="60" r={R} fill="none" strokeWidth="8" className="progress"
              strokeDasharray={C} strokeDashoffset={offset} />
          </svg>
          <div className="album-ring-center">
            <div>
              <div className="album-ring-pct">{loggedIn ? `${porcentaje}%` : '0%'}</div>
              <div className="album-ring-lbl">Completo</div>
            </div>
          </div>
        </div>
        <div className="album-stats">
          <div className="album-stat">
            <span className="album-stat-label">Cromos</span>
            <span className="album-stat-val mono">
              {loggedIn ? laminasObtenidas : 0} / {totalLaminas || '—'}
            </span>
          </div>
        </div>
      </div>
      <button className="album-pack" onClick={loggedIn ? () => onToast('Abriendo sobre…') : onLogin}>
        <div className="pack-info">
          <div className="pack-icon"></div>
          <div className="pack-text">
            <div className="pack-title">{loggedIn ? 'Abrir un sobre' : 'Sobre de bienvenida'}</div>
            <div className="pack-sub">{loggedIn ? 'Colecciona jugadores del Mundial' : 'Regístrate y llévatelo'}</div>
          </div>
        </div>
        <span className="match-cta">{loggedIn ? 'Abrir' : 'Reclamar'} <Icon name="arrow" size={12} /></span>
      </button>
    </div>
  );
};

// ======= POLLA PANEL =======
const PollaPanel = ({ loggedIn, onLogin }) => (
  <div className="panel" id="pollas">
    <div className="panel-head">
      <div>
        <div className="panel-title">Mis predicciones</div>
        <div className="panel-sub">{loggedIn ? 'Tus pollas activas' : 'Únete a una polla'}</div>
      </div>
    </div>
    {loggedIn ? (
      <div style={{ textAlign: 'center', padding: '28px 0' }}>
        <div style={{ fontSize: 36, marginBottom: 12 }}>🏆</div>
        <div style={{ fontSize: 15, fontWeight: 600, marginBottom: 6 }}>Sin predicciones aún</div>
        <div style={{ fontSize: 13, color: 'var(--ink-3)' }}>
          Aquí aparecerán tus predicciones de polla cuando estén disponibles.
        </div>
      </div>
    ) : (
      <div style={{ textAlign: 'center', padding: '24px 0' }}>
        <div style={{ fontSize: 40, marginBottom: 12 }}>🏆</div>
        <div style={{ fontSize: 16, fontWeight: 600, marginBottom: 6 }}>Crea o únete a una polla</div>
        <div style={{ fontSize: 13, color: 'var(--ink-3)', marginBottom: 20, maxWidth: 280, margin: '0 auto 20px' }}>
          Compite con amigos prediciendo resultados del Mundial.
        </div>
        <button className="btn btn-accent btn-lg" onClick={onLogin}>Empezar</button>
      </div>
    )}
  </div>
);

// ======= GROUP CARD =======
const GroupCard = ({ letter, rows, status }) => (
  <div className="group-card">
    <div className="group-head">
      <div className="group-letter">Grupo {letter}</div>
      <div className="group-status">{status}</div>
    </div>
    <div className="group-row header">
      <span className="num">#</span><span></span>
      <span>Equipo</span>
      <span className="tnum">PJ</span><span className="tnum">DG</span><span className="tnum">GF</span>
      <span className="pts">PTS</span>
    </div>
    {rows.map((r, i) => (
      <div key={r.team.code} className={'group-row' + (r.qualified ? ' qualified' : '')}>
        <span className="num">{i + 1}</span>
        <span className="flag"><TeamFlag iso2={r.team.iso2} size="row" /></span>
        <span className="tname">{r.team.name}</span>
        <span className="tnum">{r.pj}</span>
        <span className="tnum">{r.gf - r.gc > 0 ? '+' : ''}{r.gf - r.gc}</span>
        <span className="tnum">{r.gf}</span>
        <span className="pts">{r.pts}</span>
      </div>
    ))}
  </div>
);

// ======= ACTIVITY =======
const Activity = ({ loggedIn }) => {
  if (!loggedIn) return null;
  return (
    <section className="section container">
      <div className="section-head">
        <div>
          <h2 className="section-title">Tu actividad</h2>
          <div className="section-sub">Lo que has hecho últimamente</div>
        </div>
      </div>
      <div style={{ textAlign: 'center', padding: '32px 0', color: 'var(--ink-3)' }}>
        <div style={{ fontSize: 36, marginBottom: 12 }}>📋</div>
        <div style={{ fontSize: 15, fontWeight: 600, color: 'var(--ink-2)', marginBottom: 6 }}>Sin actividad reciente</div>
        <div style={{ fontSize: 13 }}>Aquí aparecerán tus acciones: partidos vistos, cromos abiertos y predicciones.</div>
      </div>
    </section>
  );
};

// ======= MAIN INICIO COMPONENT =======
const Inicio = () => {
  const { dark, toggleDark } = useDarkMode();
  const {
    loggedIn, user, modal,
    showToast,
    openLogin, openRegister, closeModal,
    handleLoginSuccess, handleRegisterSuccess, handleLogout,
  } = useAuth();

  const [upcoming, setUpcoming] = useState([]);
  const [results, setResults] = useState(MOCK_RESULTS);
  const [liveMatch, setLiveMatch] = useState(null);
  const [grupos, setGrupos] = useState([]);
  const [albumData, setAlbumData] = useState({ laminasObtenidas: 0, totalLaminas: 0, porcentaje: 0 });
  const [matchModal, setMatchModal] = useState(null);

  const handleModalAgenda = async (match) => {
    if (!loggedIn) { showToast('Inicia sesión para agregar a tu agenda'); return; }
    try {
      const u = JSON.parse(localStorage.getItem('user') || 'null');
      const uid = u?.usuario?.id || u?.id;
      if (!uid) { showToast('Error: sesión inválida, vuelve a iniciar sesión'); return; }
      await agregarAgenda(uid, match.id);
      showToast(`${match.home.name} vs ${match.away.name} agregado a tu agenda`);
      setMatchModal(null);
    } catch { showToast('Error al agregar a la agenda'); }
  };

  useEffect(() => {
    getEnJuego().then((res) => {
      const d = res?.data;
      if (Array.isArray(d) && d.length > 0) setLiveMatch(adaptLive(d[0]));
      else if (d && !Array.isArray(d)) setLiveMatch(adaptLive(d));
    }).catch(() => {});

    getProximosPartidos().then((res) => {
      const d = res?.data;
      if (Array.isArray(d) && d.length > 0) setUpcoming(d.map(adaptUpcoming));
    }).catch(() => {});

    getResultados().then((res) => {
      const d = res?.data;
      if (Array.isArray(d) && d.length > 0) setResults(d.map(adaptResult));
    }).catch(() => {});

    getTodosPartidos().then((res) => {
      const d = res?.data;
      if (Array.isArray(d) && d.length > 0) {
        const computed = computarGrupos(d);
        if (computed.length > 0) setGrupos(computed);
      }
    }).catch(() => {});
  }, []);

  const userId = user?.usuario?.id || user?.id;
  useEffect(() => {
    if (!loggedIn || !userId) return;
    getAlbumUsuario(userId).then((res) => {
      const d = res?.data;
      if (d) setAlbumData({
        laminasObtenidas: d.laminasObtenidas ?? 0,
        totalLaminas: d.totalLaminas ?? 0,
        porcentaje: d.porcentaje ?? 0,
      });
    }).catch(() => {});
  }, [loggedIn, userId]);

  return (
    <div className="app">
      <Navbar
        loggedIn={loggedIn}
        user={user}
        onLogin={openLogin}
        onRegister={openRegister}
        dark={dark}
        onToggleDark={toggleDark}
        onLogout={handleLogout}
      />

      <Hero liveMatch={liveMatch} nextMatch={upcoming[0] || null} />

      <QuickActions loggedIn={loggedIn} onToast={showToast} onLogin={openLogin} />

      <section className="section container" id="proximos">
        <div className="section-head">
          <div>
            <h2 className="section-title">Próximos partidos</h2>
            <div className="section-sub">Partidos programados</div>
          </div>
          <a className="section-link" href="/calendario">Ver calendario <Icon name="arrow" size={12} /></a>
        </div>
        {upcoming.length > 0 ? (
          <HScrollRow>
            {upcoming.map((m) => <MatchCard key={m.id} m={m} onMoreInfo={() => setMatchModal(m)} />)}
          </HScrollRow>
        ) : (
          <div style={{ padding: '28px 0', textAlign: 'center', color: 'var(--ink-3)', fontSize: 13 }}>
            No hay partidos programados en este momento.
          </div>
        )}
      </section>

      <section className="section container">
        <div className="section-head">
          <div>
            <h2 className="section-title">Resultados recientes</h2>
            <div className="section-sub">Incluye partidos en curso</div>
          </div>
          <a className="section-link" href="/calendario">Ver todos <Icon name="arrow" size={12} /></a>
        </div>
        <HScrollRow>
          {results.map((r) => <ResultCard key={r.id} r={r} />)}
        </HScrollRow>
      </section>

      {loggedIn ? (
        <section className="section container">
          <div className="dual-grid">
            <AlbumPanel loggedIn={loggedIn} onLogin={openLogin} onToast={showToast} albumData={albumData} />
            <PollaPanel loggedIn={loggedIn} onLogin={openLogin} />
          </div>
        </section>
      ) : (
        <section className="section container">
          <div className="signedout-banner">
            <div>
              <h3>Colecciona cromos, predice y compite con tus amigos.</h3>
              <p>Tu álbum digital, pollas privadas, alertas de goles y todo el Mundial en un solo lugar.</p>
            </div>
            <div className="signedout-cta">
              <button className="hero-cta primary" onClick={openRegister}>Crear cuenta</button>
              <button className="hero-cta ghost" onClick={openLogin}>Iniciar sesión</button>
            </div>
          </div>
        </section>
      )}

      <LegendsShowcase />

      <section className="section container" id="grupos">
        <div className="section-head">
          <div>
            <h2 className="section-title">Grupos destacados</h2>
            <div className="section-sub">Fase de grupos · clasificados resaltados</div>
          </div>
          <a className="section-link" href="/posiciones">Ver posiciones <Icon name="arrow" size={12} /></a>
        </div>
        {grupos.length > 0 ? (
          <div className="groups-scroll">
            {grupos.map((g) => (
              <GroupCard key={g.letra} letter={g.letra} rows={g.rows} status={g.status} />
            ))}
          </div>
        ) : (
          <div style={{ padding: '28px 0', textAlign: 'center', color: 'var(--ink-3)', fontSize: 13 }}>
            No hay datos de grupos disponibles. Sincroniza los partidos desde el backend.
          </div>
        )}
      </section>

      <Activity loggedIn={loggedIn} />

      <Footer />

      {modal === 'login' && (
        <LoginModal
          onClose={closeModal}
          onLoginSuccess={handleLoginSuccess}
          onSwitchToRegister={() => openRegister()}
        />
      )}
      {modal === 'register' && (
        <RegisterModal
          onClose={closeModal}
          onRegisterSuccess={handleRegisterSuccess}
          onSwitchToLogin={() => openLogin()}
        />
      )}
      {matchModal && (
        <MatchModal
          match={matchModal}
          loggedIn={loggedIn}
          onClose={() => setMatchModal(null)}
          onAgenda={handleModalAgenda}
        />
      )}
    </div>
  );
};

export default Inicio;
