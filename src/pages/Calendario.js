import React, { useState, useEffect, useMemo } from 'react';
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
import { PHASE_ORDER, adaptPhase } from '../utils/phases';
import { formatTime, formatDate } from '../utils/formatters';
import { getTodosLosPartidos, sincronizarPartidos } from '../services/calendarioService';
import { agregarAgenda } from '../services/partidoService';
import TeamFlag from '../components/TeamFlag';
import MatchModal from '../components/ui/MatchModal';

// ======= CONSTANTS =======
const phaseGroup = (f) => {
  if (!f) return 'group';
  if (f.startsWith('GROUP')) return 'group';
  if (f === 'LAST_32') return 'r32';
  if (f === 'LAST_16') return 'r16';
  if (f === 'QUARTER_FINALS') return 'qf';
  if (f === 'SEMI_FINALS') return 'sf';
  return 'final';
};

const VENUE_INFO = {
  'MetLife': { city: 'New York / NJ', country: 'USA', flag: '🇺🇸' },
  'SoFi': { city: 'Los Ángeles', country: 'USA', flag: '🇺🇸' },
  "Levi's": { city: 'San Francisco', country: 'USA', flag: '🇺🇸' },
  'AT&T': { city: 'Dallas', country: 'USA', flag: '🇺🇸' },
  'Hard Rock': { city: 'Miami', country: 'USA', flag: '🇺🇸' },
  'Gillette': { city: 'Boston', country: 'USA', flag: '🇺🇸' },
  'Lumen': { city: 'Seattle', country: 'USA', flag: '🇺🇸' },
  'NRG': { city: 'Houston', country: 'USA', flag: '🇺🇸' },
  'Arrowhead': { city: 'Kansas City', country: 'USA', flag: '🇺🇸' },
  'Mercedes-Benz': { city: 'Atlanta', country: 'USA', flag: '🇺🇸' },
  'Lincoln Financial': { city: 'Filadelfia', country: 'USA', flag: '🇺🇸' },
  'Azteca': { city: 'Ciudad de México', country: 'MEX', flag: '🇲🇽' },
  'Akron': { city: 'Guadalajara', country: 'MEX', flag: '🇲🇽' },
  'BBVA': { city: 'Monterrey', country: 'MEX', flag: '🇲🇽' },
  'BMO': { city: 'Toronto', country: 'CAN', flag: '🇨🇦' },
  'BC Place': { city: 'Vancouver', country: 'CAN', flag: '🇨🇦' },
};

const getVenueInfo = (estadio, ciudadBackend) => {
  if (ciudadBackend) {
    for (const [, info] of Object.entries(VENUE_INFO)) {
      if (info.city.toLowerCase().includes(ciudadBackend.toLowerCase()) ||
          ciudadBackend.toLowerCase().includes(info.city.toLowerCase())) return info;
    }
    const lo = ciudadBackend.toLowerCase();
    const country = lo.includes('mexico') || lo.includes('méxico') || lo.includes('guadalajara') || lo.includes('monterrey') ? 'MEX'
      : lo.includes('toronto') || lo.includes('vancouver') ? 'CAN' : 'USA';
    const flag = country === 'MEX' ? '🇲🇽' : country === 'CAN' ? '🇨🇦' : '🇺🇸';
    return { city: ciudadBackend, country, flag };
  }
  if (estadio && estadio !== 'Por confirmar') {
    for (const [key, info] of Object.entries(VENUE_INFO)) {
      if (estadio.toLowerCase().includes(key.toLowerCase())) return info;
    }
  }
  return { city: 'Por confirmar', country: '', flag: '🌐' };
};

// ======= ADAPTERS =======
const toDateKey = (fechaHora) => {
  if (!fechaHora) return 'sin-fecha';
  const d = new Date(fechaHora);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

const adaptPartido = (p) => {
  const vi = getVenueInfo(p.sede?.nombreEstadio, p.sede?.ciudad);
  return {
    id: p.id,
    home: { name: p.equipoLocal || '?', iso2: teamIso2(p.equipoLocal) },
    away: { name: p.equipoVisitante || '?', iso2: teamIso2(p.equipoVisitante) },
    phaseRaw: p.fase || '',
    pGroup: phaseGroup(p.fase),
    phase: adaptPhase(p.fase),
    venue: p.sede?.nombreEstadio || 'Por confirmar',
    city: vi.city,
    country: vi.country,
    countryFlag: vi.flag,
    dateKey: toDateKey(p.fechaHora),
    rawDate: p.fechaHora ? new Date(p.fechaHora) : null,
    date: formatDate(p.fechaHora),
    time: formatTime(p.fechaHora),
    status: p.estado || 'PROGRAMADO',
    golesLocal: p.golesLocal,
    golesVisitante: p.golesVisitante,
  };
};

// ======= FILTER SIDEBAR =======
const FilterSidebar = ({ filters, setFilters, availablePhases, filteredCities, totalCount, filteredCount }) => {
  const set = (k) => (e) => setFilters((f) => ({ ...f, [k]: e.target.value }));
  const activeCount = Object.values(filters).filter(Boolean).length;
  const reset = () => setFilters({ search: '', fase: '', pais: '', ciudad: '', dateFrom: '', dateTo: '', estado: '' });

  return (
    <aside className="cal-sidebar">
      <div className="cal-sidebar-header">
        <div className="cal-sidebar-title"><Icon name="filter" size={13} /> Filtros</div>
        {activeCount > 0 && (
          <button className="cal-reset-btn" onClick={reset}>
            Limpiar <span className="cal-badge">{activeCount}</span>
          </button>
        )}
      </div>

      <div className="cal-filter-group">
        <label className="cal-filter-label"><Icon name="search" size={11} /> Buscar equipo</label>
        <div className="cal-search-wrap">
          <input
            className="cal-filter-input"
            type="text"
            placeholder="México, Brasil, España…"
            value={filters.search}
            onChange={set('search')}
          />
          {filters.search && (
            <button className="cal-search-clear" onClick={() => setFilters((f) => ({ ...f, search: '' }))}>×</button>
          )}
        </div>
      </div>

      <div className="cal-filter-group">
        <label className="cal-filter-label"><Icon name="trophy" size={11} /> Fase del torneo</label>
        <select className="cal-filter-select" value={filters.fase} onChange={set('fase')}>
          <option value="">Todas las fases</option>
          {availablePhases.map((ph) => (
            <option key={ph.value} value={ph.value}>{ph.label}</option>
          ))}
        </select>
      </div>

      <div className="cal-filter-group">
        <label className="cal-filter-label"><Icon name="pin" size={11} /> País sede</label>
        <div className="cal-country-btns">
          {[
            { v: '', l: 'Todos' },
            { v: 'USA', l: '🇺🇸 USA' },
            { v: 'MEX', l: '🇲🇽 México' },
            { v: 'CAN', l: '🇨🇦 Canadá' },
          ].map((o) => (
            <button
              key={o.v}
              className={'cal-country-btn' + (filters.pais === o.v ? ' active' : '')}
              onClick={() => setFilters((f) => ({ ...f, pais: o.v, ciudad: '' }))}
            >
              {o.l}
            </button>
          ))}
        </div>
      </div>

      {filteredCities.length > 0 && (
        <div className="cal-filter-group">
          <label className="cal-filter-label"><Icon name="stadium" size={11} /> Ciudad / Sede</label>
          <select className="cal-filter-select" value={filters.ciudad} onChange={set('ciudad')}>
            <option value="">Todas las ciudades</option>
            {filteredCities.map((c) => (
              <option key={c.value} value={c.value}>{c.label}</option>
            ))}
          </select>
        </div>
      )}

      <div className="cal-filter-group">
        <label className="cal-filter-label"><Icon name="cal" size={11} /> Rango de fechas</label>
        <div className="cal-date-range">
          <input
            className="cal-filter-input cal-date-input"
            type="date"
            value={filters.dateFrom}
            min="2026-06-11"
            max="2026-07-19"
            onChange={set('dateFrom')}
          />
          <span className="cal-date-sep">→</span>
          <input
            className="cal-filter-input cal-date-input"
            type="date"
            value={filters.dateTo}
            min="2026-06-11"
            max="2026-07-19"
            onChange={set('dateTo')}
          />
        </div>
      </div>

      <div className="cal-filter-group">
        <label className="cal-filter-label"><Icon name="play" size={11} /> Estado del partido</label>
        <select className="cal-filter-select" value={filters.estado} onChange={set('estado')}>
          <option value="">Todos</option>
          <option value="PROGRAMADO">Programado</option>
          <option value="EN_JUEGO">En juego</option>
          <option value="FINALIZADO">Finalizado</option>
          <option value="POSPUESTO">Pospuesto</option>
        </select>
      </div>

      <div className="cal-sidebar-count">
        <span className="mono" style={{ fontWeight: 700, color: 'var(--accent)' }}>{filteredCount}</span>
        {' '}de{' '}
        <span className="mono">{totalCount}</span> partidos
      </div>
    </aside>
  );
};

// ======= MATCH ROW =======
const MatchRow = ({ m, idx, loggedIn, onAgenda, onDetail }) => {
  const isLive = m.status === 'EN_JUEGO';
  const isDone = m.status === 'FINALIZADO';
  const hlWin = isDone || isLive ? (m.golesLocal > m.golesVisitante ? 'win' : m.golesLocal < m.golesVisitante ? 'loss' : 'draw') : null;
  const awWin = isDone || isLive ? (m.golesVisitante > m.golesLocal ? 'win' : m.golesVisitante < m.golesLocal ? 'loss' : 'draw') : null;

  return (
    <div
      className={'cal-match-item' + (isLive ? ' live' : isDone ? ' done' : '')}
      style={{ animationDelay: `${idx * 0.03}s`, cursor: 'pointer' }}
      onClick={() => onDetail && onDetail(m)}
    >
      <div className="cal-match-time">
        <span className="cal-time-val mono">{m.time || '--:--'}</span>
        {isLive && <span className="cal-live-dot"></span>}
        {m.countryFlag && m.country !== '' && (
          <span style={{ fontSize: 14 }}>{m.countryFlag}</span>
        )}
      </div>

      <div className="cal-match-teams">
        <div className="cal-team home">
          <span className={'cal-team-name' + (hlWin === 'win' ? ' winner' : hlWin === 'loss' ? ' loser' : '')}>
            {m.home.name}
          </span>
          <div className="cal-flag"><TeamFlag iso2={m.home.iso2} size="card" /></div>
        </div>

        <div className="cal-match-center">
          {isDone || isLive ? (
            <div className="cal-score">
              <span className={hlWin === 'loss' ? 'loser' : ''}>{m.golesLocal ?? 0}</span>
              <span className="cal-score-sep">—</span>
              <span className={awWin === 'loss' ? 'loser' : ''}>{m.golesVisitante ?? 0}</span>
            </div>
          ) : (
            <div className="cal-vs">VS</div>
          )}
        </div>

        <div className="cal-team away">
          <div className="cal-flag"><TeamFlag iso2={m.away.iso2} size="card" /></div>
          <span className={'cal-team-name' + (awWin === 'win' ? ' winner' : awWin === 'loss' ? ' loser' : '')}>
            {m.away.name}
          </span>
        </div>
      </div>

      <div className="cal-match-meta">
        <span className={'cal-phase-badge ' + m.pGroup}>{m.phase}</span>
        {m.city && m.city !== 'Por confirmar' && (
          <span className="cal-venue-text">
            <Icon name="pin" size={11} /> {m.city}
          </span>
        )}
        {isLive && (
          <span className="cal-status-live">
            <span className="cal-live-dot"></span> EN VIVO
          </span>
        )}
      </div>

      {loggedIn && (
        <button className="cal-agenda-btn" onClick={(e) => { e.stopPropagation(); onAgenda(m); }} title="Agregar a mi agenda">
          <Icon name="plus" size={13} />
        </button>
      )}
    </div>
  );
};

// ======= LIST VIEW =======
const ListView = ({ grouped, loggedIn, onAgenda, onDetail }) => {
  const todayKey = new Date().toISOString().split('T')[0];
  const MESES = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];
  const DIAS = ['domingo','lunes','martes','miércoles','jueves','viernes','sábado'];

  return (
    <div className="cal-list">
      {grouped.map(([dateKey, matches], gi) => {
        const date = dateKey === 'sin-fecha' ? null : new Date(dateKey + 'T12:00:00');
        const isToday = dateKey === todayKey;
        const dayNum = date ? date.getDate() : '?';
        const weekDay = date ? DIAS[date.getDay()] : '';
        const monthLbl = date ? `${MESES[date.getMonth()]} ${date.getFullYear()}` : '';

        return (
          <div key={dateKey} className="cal-date-group" style={{ animationDelay: `${gi * 0.05}s` }}>
            <div className={'cal-date-hdr' + (isToday ? ' today' : '')}>
              <div className="cal-date-hdr-num">{dayNum}</div>
              <div className="cal-date-hdr-info">
                <div className="cal-date-weekday">
                  {weekDay.charAt(0).toUpperCase() + weekDay.slice(1)}
                </div>
                <div className="cal-date-month">{monthLbl}</div>
              </div>
              <div className="cal-date-count">
                {matches.length} partido{matches.length !== 1 ? 's' : ''}
              </div>
              {isToday && <div className="cal-today-badge">Hoy</div>}
            </div>
            <div className="cal-match-list">
              {matches.map((m, i) => (
                <MatchRow key={m.id} m={m} idx={i} loggedIn={loggedIn} onAgenda={onAgenda} onDetail={onDetail} />
              ))}
            </div>
          </div>
        );
      })}
    </div>
  );
};

// ======= CALENDAR GRID VIEW =======
const CalendarView = ({ filtered, loggedIn, onAgenda, onDetail }) => {
  const [calYear, setCalYear] = useState(2026);
  const [calMonth, setCalMonth] = useState(5); // June (0-indexed)
  const [selectedDay, setSelectedDay] = useState(null);

  const byDate = useMemo(() => {
    const map = {};
    filtered.forEach((p) => {
      if (!map[p.dateKey]) map[p.dateKey] = [];
      map[p.dateKey].push(p);
    });
    return map;
  }, [filtered]);

  const MONTH_NAMES = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];
  const WEEK_DAYS = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];

  const daysInMonth = new Date(calYear, calMonth + 1, 0).getDate();
  const firstDayJS = new Date(calYear, calMonth, 1).getDay();
  const startOffset = firstDayJS === 0 ? 6 : firstDayJS - 1;
  const daysInPrevMonth = new Date(calYear, calMonth, 0).getDate();

  const cells = [];
  for (let i = startOffset - 1; i >= 0; i--) cells.push({ day: daysInPrevMonth - i, month: calMonth - 1, year: calYear, other: true });
  for (let d = 1; d <= daysInMonth; d++) cells.push({ day: d, month: calMonth, year: calYear, other: false });
  const remaining = Math.ceil(cells.length / 7) * 7 - cells.length;
  for (let d = 1; d <= remaining; d++) cells.push({ day: d, month: calMonth + 1, year: calYear, other: true });

  const toKey = (cell) => {
    if (cell.other) return null;
    return `${cell.year}-${String(cell.month + 1).padStart(2, '0')}-${String(cell.day).padStart(2, '0')}`;
  };

  const todayKey = new Date().toISOString().split('T')[0];
  const canPrev = !(calYear === 2026 && calMonth === 5);
  const canNext = !(calYear === 2026 && calMonth === 6);

  const prevMonth = () => { if (calMonth === 0) { setCalMonth(11); setCalYear((y) => y - 1); } else setCalMonth((m) => m - 1); };
  const nextMonth = () => { if (calMonth === 11) { setCalMonth(0); setCalYear((y) => y + 1); } else setCalMonth((m) => m + 1); };

  const selectedMatches = selectedDay ? (byDate[selectedDay] || []) : [];
  const MESES_LARGO = ['enero','febrero','marzo','abril','mayo','junio','julio','agosto','septiembre','octubre','noviembre','diciembre'];
  const DIAS_LARGO = ['domingo','lunes','martes','miércoles','jueves','viernes','sábado'];

  return (
    <div className="cal-month-wrap">
      <div className="cal-month-nav">
        <button className="cal-month-arrow" onClick={prevMonth} disabled={!canPrev}>
          <Icon name="arrow-left" size={18} />
        </button>
        <div className="cal-month-label">{MONTH_NAMES[calMonth]} {calYear}</div>
        <button className="cal-month-arrow" onClick={nextMonth} disabled={!canNext}>
          <Icon name="arrow" size={18} />
        </button>
      </div>

      <div className="cal-weekday-row">
        {WEEK_DAYS.map((d) => <div key={d} className="cal-weekday">{d}</div>)}
      </div>

      <div className="cal-grid">
        {cells.map((cell, i) => {
          const key = toKey(cell);
          const matches = key ? (byDate[key] || []) : [];
          const hasMatch = matches.length > 0;
          const isToday = key === todayKey;
          const isSelected = key === selectedDay;
          const phases = [...new Set(matches.map((m) => m.pGroup))];

          return (
            <div
              key={i}
              className={[
                'cal-cell',
                cell.other ? 'dim' : '',
                hasMatch ? 'has-match' : '',
                isToday ? 'today' : '',
                isSelected ? 'selected' : '',
              ].filter(Boolean).join(' ')}
              onClick={() => hasMatch && key && setSelectedDay(isSelected ? null : key)}
            >
              <div className="cal-cell-num">{cell.day}</div>
              {hasMatch && (
                <div className="cal-cell-matches">
                  {phases.slice(0, 5).map((ph, pi) => (
                    <span key={pi} className={'cal-cell-dot ' + ph}></span>
                  ))}
                </div>
              )}
              {hasMatch && (
                <div className="cal-cell-count">{matches.length}</div>
              )}
            </div>
          );
        })}
      </div>

      <div className="cal-legend">
        {[
          { cls: 'group', lbl: 'Fase de Grupos' },
          { cls: 'r32', lbl: '32avos' },
          { cls: 'r16', lbl: 'Octavos' },
          { cls: 'qf', lbl: 'Cuartos' },
          { cls: 'sf', lbl: 'Semis' },
          { cls: 'final', lbl: 'Final' },
        ].map((item) => (
          <div key={item.cls} className="cal-legend-item">
            <span className={'cal-cell-dot ' + item.cls}></span>
            <span>{item.lbl}</span>
          </div>
        ))}
      </div>

      {selectedDay && selectedMatches.length > 0 && (
        <div className="cal-day-detail">
          <div className="cal-day-detail-header">
            <div className="cal-day-detail-title">
              {(() => {
                const d = new Date(selectedDay + 'T12:00:00');
                return `${DIAS_LARGO[d.getDay()].charAt(0).toUpperCase() + DIAS_LARGO[d.getDay()].slice(1)}, ${d.getDate()} de ${MESES_LARGO[d.getMonth()]} de ${d.getFullYear()}`;
              })()}
            </div>
            <div className="cal-day-detail-count">
              {selectedMatches.length} partido{selectedMatches.length !== 1 ? 's' : ''}
            </div>
            <button className="cal-day-detail-close" onClick={() => setSelectedDay(null)}>
              <Icon name="close" size={14} />
            </button>
          </div>
          <div className="cal-match-list" style={{ padding: '8px' }}>
            {selectedMatches.map((m, i) => (
              <MatchRow key={m.id} m={m} idx={i} loggedIn={loggedIn} onAgenda={onAgenda} onDetail={onDetail} />
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

// ======= MAIN COMPONENT =======
const Calendario = () => {
  const { dark, toggleDark } = useDarkMode();
  const {
    loggedIn, user, modal, setToast,
    openLogin, openRegister, closeModal,
    handleLoginSuccess, handleRegisterSuccess, handleLogout,
  } = useAuth();

  const [partidos, setPartidos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);
  const [view, setView] = useState('lista');
  const [filters, setFilters] = useState({ search: '', fase: '', pais: '', ciudad: '', dateFrom: '', dateTo: '', estado: '' });
  const [matchModal, setMatchModal] = useState(null);

  useEffect(() => {
    getTodosLosPartidos()
      .then((res) => { const d = res?.data; if (Array.isArray(d)) setPartidos(d.map(adaptPartido)); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleSync = async () => {
    setSyncing(true);
    try {
      await sincronizarPartidos();
      const res = await getTodosLosPartidos();
      const d = res?.data;
      if (Array.isArray(d)) setPartidos(d.map(adaptPartido));
      setToast('Partidos sincronizados correctamente');
    } catch { setToast('No se pudo sincronizar. Verifica la conexión.'); }
    finally { setSyncing(false); }
  };

  const availablePhases = useMemo(() => {
    const seen = new Set();
    return PHASE_ORDER.filter((f) => {
      if (partidos.some((p) => p.phaseRaw === f) && !seen.has(f)) { seen.add(f); return true; }
      return false;
    }).map((f) => ({ value: f, label: adaptPhase(f) }));
  }, [partidos]);

  const availableCities = useMemo(() => {
    const seen = new Set();
    const cities = [];
    partidos.forEach((p) => {
      if (p.city && p.city !== 'Por confirmar' && !seen.has(p.city)) {
        seen.add(p.city);
        cities.push({ value: p.city, label: `${p.countryFlag} ${p.city}`, country: p.country });
      }
    });
    return cities.sort((a, b) => a.label.localeCompare(b.label));
  }, [partidos]);

  const filteredCities = useMemo(
    () => !filters.pais ? availableCities : availableCities.filter((c) => c.country === filters.pais),
    [availableCities, filters.pais]
  );

  const filtered = useMemo(() => {
    return partidos.filter((p) => {
      if (filters.search) {
        const q = filters.search.toLowerCase();
        if (!p.home.name.toLowerCase().includes(q) && !p.away.name.toLowerCase().includes(q)) return false;
      }
      if (filters.fase && p.phaseRaw !== filters.fase) return false;
      if (filters.pais && p.country !== filters.pais) return false;
      if (filters.ciudad && p.city !== filters.ciudad) return false;
      if (filters.dateFrom && p.rawDate && p.rawDate < new Date(filters.dateFrom)) return false;
      if (filters.dateTo && p.rawDate && p.rawDate > new Date(filters.dateTo + 'T23:59:59')) return false;
      if (filters.estado && p.status !== filters.estado) return false;
      return true;
    });
  }, [partidos, filters]);

  const grouped = useMemo(() => {
    const map = {};
    filtered.forEach((p) => { if (!map[p.dateKey]) map[p.dateKey] = []; map[p.dateKey].push(p); });
    return Object.entries(map)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([key, matches]) => [key, matches.sort((a, b) => (a.rawDate || 0) - (b.rawDate || 0))]);
  }, [filtered]);

  const handleAgenda = async (match) => {
    if (!loggedIn) { setToast('Inicia sesión para agregar a tu agenda'); return; }
    try {
      const u = JSON.parse(localStorage.getItem('user') || 'null');
      const uid = u?.usuario?.id || u?.id;
      if (!uid) { setToast('Error: sesión inválida, vuelve a iniciar sesión'); return; }
      await agregarAgenda(uid, match.id);
      setToast(`${match.home.name} vs ${match.away.name} agregado a tu agenda`);
    } catch { setToast('Error al agregar a la agenda'); }
  };

  const hasFilters = Object.values(filters).some(Boolean);

  const handleModalAgenda = async (match) => {
    if (!loggedIn) { setToast('Inicia sesión para agregar a tu agenda'); return; }
    try {
      const u = JSON.parse(localStorage.getItem('user') || 'null');
      const uid = u?.usuario?.id || u?.id;
      if (!uid) { setToast('Error: sesión inválida, vuelve a iniciar sesión'); return; }
      await agregarAgenda(uid, match.id);
      setToast(`${match.home.name} vs ${match.away.name} agregado a tu agenda`);
      setMatchModal(null);
    } catch { setToast('Error al agregar a la agenda'); }
  };

  return (
    <div className="app">
      <Navbar
        dark={dark} onToggleDark={toggleDark}
        loggedIn={loggedIn} user={user}
        onLogin={openLogin} onRegister={openRegister} onLogout={handleLogout}
      />

      <PageHero
        badge="Calendario Oficial · FIFA World Cup 2026™"
        badgeIcon="cal"
        title={<>104 Partidos<br />Una Copa del Mundo</>}
        subtitle="Filtra por sede, equipo, fase y fechas. Agrega partidos a tu agenda personal."
        stats={[
          { num: '104', lbl: 'Partidos' },
          { num: '16',  lbl: 'Sedes'    },
          { num: '3',   lbl: 'Países'   },
          { num: '48',  lbl: 'Equipos'  },
        ]}
        variant="calendario"
        mediaKey="calendario"
      />

      {/* ── Main layout ── */}
      <div className="container">
        <div className="cal-layout">
          <FilterSidebar
            filters={filters}
            setFilters={setFilters}
            availablePhases={availablePhases}
            filteredCities={filteredCities}
            totalCount={partidos.length}
            filteredCount={filtered.length}
          />

          <div className="cal-content">
            {/* Toolbar */}
            <div className="cal-toolbar">
              <div className="cal-result-info">
                <span className="cal-result-count">{filtered.length}</span>
                <span className="cal-result-lbl"> partido{filtered.length !== 1 ? 's' : ''}</span>
                {hasFilters && filtered.length < partidos.length && (
                  <span className="cal-result-filtered"> · {partidos.length - filtered.length} ocultos</span>
                )}
              </div>
              <div className="cal-toolbar-right">
                <button
                  className={'cal-sync-btn' + (syncing ? ' loading' : '')}
                  onClick={handleSync}
                  disabled={syncing}
                  title="Sincronizar partidos con la API"
                >
                  <Icon name="refresh" size={13} />
                  {syncing ? 'Sincronizando…' : 'Sincronizar'}
                </button>
                <div className="cal-view-toggle">
                  <button className={'cal-vbtn' + (view === 'lista' ? ' active' : '')} onClick={() => setView('lista')}>
                    <Icon name="list" size={14} /> Lista
                  </button>
                  <button className={'cal-vbtn' + (view === 'calendario' ? ' active' : '')} onClick={() => setView('calendario')}>
                    <Icon name="grid" size={14} /> Calendario
                  </button>
                </div>
              </div>
            </div>

            {/* Content area */}
            {loading ? (
              <div className="cal-loading">
                <div className="spinner"></div>
                <p>Cargando partidos del torneo…</p>
              </div>
            ) : partidos.length === 0 ? (
              <div className="cal-empty">
                <div className="cal-empty-icon"><Icon name="stadium" size={40} /></div>
                <div className="cal-empty-title">Sin partidos disponibles</div>
                <div className="cal-empty-sub">Sincroniza con la API para obtener los 104 partidos.</div>
                <button className="btn btn-primary" style={{ marginTop: 16 }} onClick={handleSync} disabled={syncing}>
                  <Icon name="refresh" size={14} /> {syncing ? 'Sincronizando…' : 'Sincronizar ahora'}
                </button>
              </div>
            ) : filtered.length === 0 ? (
              <div className="cal-empty">
                <div className="cal-empty-icon"><Icon name="search" size={40} /></div>
                <div className="cal-empty-title">Sin resultados</div>
                <div className="cal-empty-sub">No hay partidos que coincidan con los filtros aplicados.</div>
                <button className="btn btn-ghost" style={{ marginTop: 16, border: '1.5px solid var(--line)' }}
                  onClick={() => setFilters({ search: '', fase: '', pais: '', ciudad: '', dateFrom: '', dateTo: '', estado: '' })}>
                  Limpiar filtros
                </button>
              </div>
            ) : view === 'lista' ? (
              <ListView grouped={grouped} loggedIn={loggedIn} onAgenda={handleAgenda} onDetail={setMatchModal} />
            ) : (
              <CalendarView filtered={filtered} loggedIn={loggedIn} onAgenda={handleAgenda} onDetail={setMatchModal} />
            )}
          </div>
        </div>
      </div>

      <Footer />

      {modal === 'login' && (
        <LoginModal onClose={closeModal} onLoginSuccess={handleLoginSuccess} onSwitchToRegister={openRegister} />
      )}
      {modal === 'register' && (
        <RegisterModal onClose={closeModal} onRegisterSuccess={handleRegisterSuccess} onSwitchToLogin={openLogin} />
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

export default Calendario;
