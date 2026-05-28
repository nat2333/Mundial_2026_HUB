import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, CircleMarker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import Navbar from '../components/layout/Navbar';
import Footer from '../components/layout/Footer';
import Icon from '../components/ui/Icon';
import LoginModal from '../components/auth/LoginModal';
import PageHero from '../components/common/PageHero';
import RegisterModal from '../components/auth/RegisterModal';
import useDarkMode from '../hooks/useDarkMode';
import useAuth from '../hooks/useAuth';
import { getAllSedes } from '../services/sedesService';

// ─── Constants ────────────────────────────────────────────────────────────────
const COUNTRY_META = {
  'USA':    { label: 'Estados Unidos', emoji: '🇺🇸', color: '#3c78d8', accent: 'rgba(60,120,216,0.13)' },
  'México': { label: 'México',         emoji: '🇲🇽', color: '#009247', accent: 'rgba(0,146,71,0.13)'  },
  'Canadá': { label: 'Canadá',         emoji: '🇨🇦', color: '#d52b1e', accent: 'rgba(213,43,30,0.13)' },
};
const FILTROS = ['Todos', 'USA', 'México', 'Canadá'];
const fmt = (n) => n ? Number(n).toLocaleString('es-CO') : '–';

// ─── Card Image: crossfade a la segunda imagen en hover ──────────────────────
const ImgCarousel = ({ urls, alt, height, hovered }) => {
  const list = urls.filter(Boolean);

  if (!list.length) {
    return (
      <div className="sede-img-fallback" style={{ height }}>
        <Icon name="stadium" size={52} />
      </div>
    );
  }

  return (
    <div className="sede-img-wrap" style={{ height }}>
      <img src={list[0]} alt={alt} className="sede-img sede-img-base" />
      {list.length > 1 && (
        <img
          src={list[1]}
          alt={alt}
          className="sede-img sede-img-alt"
          style={{ opacity: hovered ? 1 : 0 }}
        />
      )}
    </div>
  );
};

// ─── Modal Image: navegación manual con flechas, sin auto-ciclo ──────────────
const ModalCarousel = ({ urls, alt, height }) => {
  const list = urls.filter(Boolean);
  const [idx, setIdx] = useState(0);

  const go = (dir, e) => {
    e.stopPropagation();
    setIdx(i => (i + dir + list.length) % list.length);
  };

  if (!list.length) {
    return (
      <div className="sede-img-fallback" style={{ height }}>
        <Icon name="stadium" size={52} />
      </div>
    );
  }

  return (
    <div className="sede-img-wrap" style={{ height }}>
      <img src={list[idx]} alt={alt} className="sede-img" key={idx} />
      {list.length > 1 && (
        <>
          <button className="carousel-arrow left modal-arrow" onClick={e => go(-1, e)} aria-label="Anterior">
            <Icon name="chevron-l" size={13} />
          </button>
          <button className="carousel-arrow right modal-arrow" onClick={e => go(1, e)} aria-label="Siguiente">
            <Icon name="chevron-r" size={13} />
          </button>
          <div className="carousel-dots">
            {list.map((_, i) => (
              <span
                key={i}
                className={'carousel-dot' + (i === idx ? ' active' : '')}
                onClick={e => { e.stopPropagation(); setIdx(i); }}
              />
            ))}
          </div>
        </>
      )}
    </div>
  );
};

// ─── Skeleton Card ────────────────────────────────────────────────────────────
const SkeletonCard = () => (
  <div className="sede-card sede-skel" aria-hidden="true">
    <div className="sede-skel-img skel-pulse" />
    <div className="sede-skel-bar skel-pulse" />
    <div className="sede-card-body">
      <div className="skel-line skel-pulse" style={{ width: '55%', height: 11, marginBottom: 10 }} />
      <div className="skel-line skel-pulse" style={{ width: '85%', height: 20, marginBottom: 12 }} />
      <div className="skel-line skel-pulse" style={{ width: '40%', height: 11, marginBottom: 8 }} />
      <div className="skel-line skel-pulse" style={{ width: '95%', height: 11, marginBottom: 6 }} />
      <div className="skel-line skel-pulse" style={{ width: '70%', height: 11 }} />
    </div>
  </div>
);

// ─── Sede Card ────────────────────────────────────────────────────────────────
const SedeCard = ({ sede, onClick }) => {
  const [hovered, setHovered] = useState(false);
  const meta = COUNTRY_META[sede.pais] || { label: sede.pais, emoji: '🌍', color: '#304ffe', accent: 'rgba(48,79,254,0.12)' };
  const imgs = sede.imagenUrl ? sede.imagenUrl.split(',').map(u => u.trim()) : [];

  return (
    <article
      className="sede-card"
      style={{ '--c-accent': meta.color, '--c-accent-bg': meta.accent }}
      onClick={() => onClick(sede)}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      tabIndex={0}
      onKeyDown={e => e.key === 'Enter' && onClick(sede)}
      role="button"
      aria-label={`Ver detalles de ${sede.nombreEstadio}`}
    >
      <ImgCarousel urls={imgs} alt={sede.nombreEstadio} height={210} hovered={hovered} />

      <div className="sede-card-country-bar" style={{ background: meta.accent }}>
        <span className="sede-country-chip">
          <span>{meta.emoji}</span>
          <span>{meta.label}</span>
        </span>
        <span className="sede-cap-chip">
          <Icon name="users" size={11} />
          {fmt(sede.capacidad)}
        </span>
      </div>

      <div className="sede-card-body">
        <h3 className="sede-card-name">{sede.nombreEstadio}</h3>
        <p className="sede-card-city">
          <Icon name="pin" size={12} />
          {sede.ciudad}
        </p>
        {sede.descripcion && (
          <p className="sede-card-desc">{sede.descripcion}</p>
        )}
        <div className="sede-card-cta">
          Ver sede <Icon name="arrow-r" size={13} />
        </div>
      </div>
    </article>
  );
};

// ─── Sede Modal ───────────────────────────────────────────────────────────────
const SedeModal = ({ sede, onClose }) => {
  const meta = COUNTRY_META[sede.pais] || { label: sede.pais, emoji: '🌍', color: '#304ffe', accent: 'rgba(48,79,254,0.12)' };
  const imgs = sede.imagenUrl ? sede.imagenUrl.split(',').map(u => u.trim()) : [];

  useEffect(() => {
    const handler = (e) => e.key === 'Escape' && onClose();
    document.addEventListener('keydown', handler);
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', handler);
      document.body.style.overflow = '';
    };
  }, [onClose]);

  return (
    <div className="sede-modal-overlay" onClick={onClose} role="dialog" aria-modal="true">
      <div className="sede-modal" onClick={e => e.stopPropagation()}>
        <button className="sede-modal-close" onClick={onClose} aria-label="Cerrar">
          <Icon name="close" size={18} />
        </button>

        <ModalCarousel urls={imgs} alt={sede.nombreEstadio} height={290} />

        <div className="sede-modal-body">
          <div className="sede-modal-header">
            <div className="sede-modal-country" style={{ color: meta.color }}>
              {meta.emoji} {meta.label}
            </div>
            <h2 className="sede-modal-name">{sede.nombreEstadio}</h2>
            <p className="sede-modal-city">
              <Icon name="pin" size={14} /> {sede.ciudad}, {sede.pais}
            </p>
          </div>

          <div className="sede-modal-stats">
            <div className="sede-modal-stat">
              <span className="sede-modal-stat-label">Capacidad</span>
              <span className="sede-modal-stat-val" style={{ color: meta.color }}>{fmt(sede.capacidad)}</span>
            </div>
            <div className="sede-modal-stat">
              <span className="sede-modal-stat-label">Ciudad</span>
              <span className="sede-modal-stat-val">{sede.ciudad}</span>
            </div>
            <div className="sede-modal-stat">
              <span className="sede-modal-stat-label">País sede</span>
              <span className="sede-modal-stat-val">{sede.pais}</span>
            </div>
            {sede.latitud && (
              <div className="sede-modal-stat">
                <span className="sede-modal-stat-label">Coordenadas</span>
                <span className="sede-modal-stat-val mono">
                  {Number(sede.latitud).toFixed(4)}, {Number(sede.longitud).toFixed(4)}
                </span>
              </div>
            )}
          </div>

          {sede.descripcion && (
            <p className="sede-modal-desc">{sede.descripcion}</p>
          )}
        </div>
      </div>
    </div>
  );
};

// ─── Map View ─────────────────────────────────────────────────────────────────
const MapView = ({ sedes, dark, onCardClick }) => (
  <div className="sedes-map-wrap">
    <MapContainer
      center={[32, -100]}
      zoom={4}
      scrollWheelZoom={false}
      className="sedes-map"
    >
      <TileLayer
        attribution='&copy; <a href="https://carto.com/" target="_blank">CartoDB</a>'
        url={
          dark
            ? 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png'
            : 'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png'
        }
      />
      {sedes.map(s => {
        if (!s.latitud || !s.longitud) return null;
        const meta = COUNTRY_META[s.pais] || { color: '#304ffe' };
        return (
          <CircleMarker
            key={s.id}
            center={[s.latitud, s.longitud]}
            radius={11}
            pathOptions={{
              color: '#fff', weight: 2,
              fillColor: meta.color, fillOpacity: 0.9,
            }}
          >
            <Popup>
              <div style={{ minWidth: 170, fontFamily: 'Noto Sans, sans-serif' }}>
                <strong style={{ fontSize: 14, display: 'block', marginBottom: 4, color: '#10164f' }}>
                  {s.nombreEstadio}
                </strong>
                <span style={{ fontSize: 12, color: '#5a609a' }}>
                  📍 {s.ciudad} · {fmt(s.capacidad)} esp.
                </span>
                <br />
                <button
                  style={{
                    marginTop: 10, fontSize: 12, cursor: 'pointer',
                    color: '#fff', background: meta.color,
                    border: 'none', borderRadius: 6,
                    padding: '5px 12px', fontWeight: 700, width: '100%',
                  }}
                  onClick={() => onCardClick(s)}
                >
                  Ver detalles →
                </button>
              </div>
            </Popup>
          </CircleMarker>
        );
      })}
    </MapContainer>
  </div>
);

// ─── Empty State ──────────────────────────────────────────────────────────────
const EmptyState = ({ busqueda, filtro }) => (
  <div className="sedes-empty">
    <Icon name="stadium" size={52} />
    <p className="sedes-empty-title">Sin resultados</p>
    <p className="sedes-empty-sub">
      {busqueda
        ? `No hay sedes que coincidan con "${busqueda}"`
        : `No hay sedes registradas para ${filtro}`}
    </p>
  </div>
);

// ─── Main Page ────────────────────────────────────────────────────────────────
const Sedes = () => {
  const { dark, toggleDark } = useDarkMode();
  const {
    loggedIn, user, modal,
    openLogin, openRegister, closeModal,
    handleLoginSuccess, handleRegisterSuccess, handleLogout,
  } = useAuth();
  const [sedes, setSedes]       = useState([]);
  const [loading, setLoading]   = useState(true);
  const [view, setView]         = useState('cards');
  const [filtro, setFiltro]     = useState('Todos');
  const [busqueda, setBusqueda] = useState('');
  const [selected, setSelected] = useState(null);

  useEffect(() => {
    getAllSedes()
      .then(res => setSedes(Array.isArray(res.data) ? res.data : []))
      .catch(() => setSedes([]))
      .finally(() => setLoading(false));
  }, []);

  const filtered = sedes
    .filter(s => filtro === 'Todos' || s.pais === filtro)
    .filter(s => {
      if (!busqueda) return true;
      const q = busqueda.toLowerCase();
      return (
        s.nombreEstadio?.toLowerCase().includes(q) ||
        s.ciudad?.toLowerCase().includes(q) ||
        s.pais?.toLowerCase().includes(q)
      );
    });

  const counts = FILTROS.reduce((acc, f) => {
    acc[f] = f === 'Todos' ? sedes.length : sedes.filter(s => s.pais === f).length;
    return acc;
  }, {});

  return (
    <div className="app">
      <Navbar
        dark={dark} onToggleDark={toggleDark}
        loggedIn={loggedIn} user={user}
        onLogin={openLogin}
        onRegister={openRegister}
        onLogout={handleLogout}
      />

      <PageHero
        badge="FIFA World Cup 2026™"
        badgeIcon="stadium" // o el ícono que prefieras para sedes
        title={<>SEDES<br />MUNDIALISTAS</>}
        subtitle="16 estadios icónicos en 3 países anfitriones para el torneo más grande de la historia del fútbol"
        stats={[
          { num: '11', lbl: ' USA' },
          { num: '3',  lbl: '🇲🇽 México' },
          { num: '2',  lbl: '🇨🇦 Canadá' },
          { num: '16', lbl: 'Estadios' },
        ]}
        variant="sedes"
        mediaKey="sedes"
      />

      {/* ── Controls ─────────────────────────────────────────── */}
      <div className="sedes-controls-wrap">
        <div className="container sedes-controls">
          <div className="sedes-country-tabs">
            {FILTROS.map(f => (
              <button
                key={f}
                className={'sedes-tab' + (filtro === f ? ' active' : '')}
                onClick={() => setFiltro(f)}
                style={filtro === f && COUNTRY_META[f] ? { '--tab-color': COUNTRY_META[f].color } : {}}
              >
                {COUNTRY_META[f]?.emoji ?? ''} {f}
                <span className="sedes-tab-count">{counts[f]}</span>
              </button>
            ))}
          </div>

          <div className="sedes-search-wrap">
            <Icon name="search" size={14} />
            <input
              className="sedes-search"
              placeholder="Buscar estadio o ciudad…"
              value={busqueda}
              onChange={e => setBusqueda(e.target.value)}
            />
          </div>

          <div className="sedes-view-toggle">
            <button
              className={'sedes-view-btn' + (view === 'cards' ? ' active' : '')}
              onClick={() => setView('cards')}
              title="Vista cards"
            >
              <Icon name="grid" size={14} /> Cards
            </button>
            <button
              className={'sedes-view-btn' + (view === 'mapa' ? ' active' : '')}
              onClick={() => setView('mapa')}
              title="Vista mapa"
            >
              <Icon name="map" size={14} /> Mapa
            </button>
          </div>
        </div>
      </div>

      {/* ── Content ──────────────────────────────────────────── */}
      <main className="container sedes-main">
        {view === 'cards' && (
          loading ? (
            <div className="sedes-grid">
              {Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)}
            </div>
          ) : filtered.length === 0 ? (
            <EmptyState busqueda={busqueda} filtro={filtro} />
          ) : (
            <div className="sedes-grid">
              {filtered.map(s => (
                <SedeCard key={s.id} sede={s} onClick={setSelected} />
              ))}
            </div>
          )
        )}

        {view === 'mapa' && (
          <MapView
            sedes={filtered.length ? filtered : sedes}
            dark={dark}
            onCardClick={s => { setSelected(s); }}
          />
        )}
      </main>

      <Footer />

      {selected && <SedeModal sede={selected} onClose={() => setSelected(null)} />}

      {modal === 'login'    && <LoginModal    onClose={closeModal} onLoginSuccess={handleLoginSuccess}       onSwitchToRegister={openRegister} />}
      {modal === 'register' && <RegisterModal onClose={closeModal} onRegisterSuccess={handleRegisterSuccess} onSwitchToLogin={openLogin}       />}
    </div>
  );
};

export default Sedes;
