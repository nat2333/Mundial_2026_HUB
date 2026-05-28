import React, { useState, useEffect, useCallback, useRef, useMemo } from 'react';
import Navbar from '../components/layout/Navbar';
import Footer from '../components/layout/Footer';
import Icon from '../components/ui/Icon';
import LoginModal from '../components/auth/LoginModal';
import PageHero from '../components/common/PageHero';
import RegisterModal from '../components/auth/RegisterModal';
import useDarkMode from '../hooks/useDarkMode';
import useAuth from '../hooks/useAuth';
import TeamFlag from '../components/TeamFlag';
import { teamIso2 } from '../utils/countries';
import { adaptPhase } from '../utils/phases';
import {
  getTodosPartidos,
  getEntradasPorTitular,
  getEntradasPorEstado,
  reservarEntrada,
  confirmarPago,
  getFacturaPorEntrada,
  descargarFacturaPdf,
  enviarFacturaCorreo,
  transferirEntrada,
  solicitarReembolso,
  getReembolsosPorUsuario,
  getUsuarioById,
} from '../services/entradaService';

// ─── Helpers ──────────────────────────────────────────────────────────────────
const fmtDate = (d) => {
  if (!d) return '–';
  return new Date(d).toLocaleDateString('es-CO', {
    weekday: 'short', day: 'numeric', month: 'short', year: 'numeric',
  });
};
const fmtDateTime = (d) => {
  if (!d) return '–';
  const f = new Date(d);
  return (
    f.toLocaleDateString('es-CO', { weekday: 'short', day: 'numeric', month: 'short' }) +
    ' · ' +
    f.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' })
  );
};
const fmtMoney = (n) =>
  n != null ? '$ ' + Number(n).toLocaleString('es-CO', { minimumFractionDigits: 0 }) : '–';
const capitalize = (s) =>
  s ? s.charAt(0).toUpperCase() + s.slice(1).toLowerCase() : '';
const genTxnId = () =>
  'TXN-' + Date.now().toString(36).toUpperCase() + '-' + Math.random().toString(36).slice(2, 6).toUpperCase();

const getVenueCountry = (ciudad) => {
  if (!ciudad) return '';
  const lo = ciudad.toLowerCase();
  if (lo.includes('mexico') || lo.includes('méxico') || lo.includes('guadalajara') || lo.includes('monterrey')) return 'MEX';
  if (lo.includes('toronto') || lo.includes('vancouver')) return 'CAN';
  return 'USA';
};

// ─── Helpers de validación de tarjeta ────────────────────────────────────────
const CARD_META = {
  visa:       { regex: /^4/,             cvvLen: 3, label: 'Visa',               color: '#1a1f71' },
  mastercard: { regex: /^5[1-5]|^2[2-7]/, cvvLen: 3, label: 'Mastercard',         color: '#eb001b' },
  amex:       { regex: /^3[47]/,         cvvLen: 4, label: 'American Express',   color: '#007bc1' },
};
const luhn = (num) => {
  const d = num.replace(/\D/g, '').split('').reverse().map(Number);
  if (d.length < 13) return false;
  return d.reduce((s, v, i) => { if (i % 2 === 1) { v *= 2; if (v > 9) v -= 9; } return s + v; }, 0) % 10 === 0;
};
const detectCard = (num) => {
  const n = num.replace(/\s/g, '');
  for (const [type, meta] of Object.entries(CARD_META)) if (meta.regex.test(n)) return type;
  return null;
};
const fmtCardNum = (v) => v.replace(/\D/g, '').slice(0, 16).replace(/(.{4})/g, '$1 ').trim();
const fmtExpiry  = (v) => { const d = v.replace(/\D/g, '').slice(0, 4); return d.length > 2 ? d.slice(0, 2) + '/' + d.slice(2) : d; };
const validExpiry = (v) => {
  const [mm, yy] = v.split('/');
  if (!mm || !yy || yy.length < 2) return false;
  const mo = parseInt(mm, 10), yr = 2000 + parseInt(yy, 10);
  if (mo < 1 || mo > 12) return false;
  const now = new Date();
  return new Date(yr, mo - 1, 1) >= new Date(now.getFullYear(), now.getMonth(), 1);
};

// ─── Estado badge ─────────────────────────────────────────────────────────────
const ESTADO_META = {
  DISPONIBLE:  { label: 'Disponible',  cls: 'disponible' },
  RESERVADA:   { label: 'Reservada',   cls: 'reservada'  },
  PAGADA:      { label: 'Confirmada',  cls: 'pagada'     },
  EXPIRADA:    { label: 'Expirada',    cls: 'expirada'   },
  TRANSFERIDA: { label: 'Transferida', cls: 'transferida'},
  REEMBOLSADA: { label: 'Reembolsada', cls: 'reembolsada'},
};

const EstadoBadge = ({ estado }) => {
  const meta = ESTADO_META[estado] || { label: estado, cls: 'default' };
  return (
    <span className={`entradas-estado-badge ${meta.cls}`}>
      <span className="entradas-estado-dot" />
      {meta.label}
    </span>
  );
};

// ─── Countdown timer ──────────────────────────────────────────────────────────
const CountdownTimer = ({ fechaExpiracion, onExpired }) => {
  const [display, setDisplay] = useState('--:--');
  const [urgent, setUrgent]   = useState(false);
  const [expired, setExpired] = useState(false);
  const cbRef = useRef(onExpired);
  const firedRef = useRef(false);
  useEffect(() => { cbRef.current = onExpired; }, [onExpired]);

  useEffect(() => {
    if (!fechaExpiracion) return;
    firedRef.current = false;
    const tick = () => {
      const diff = new Date(fechaExpiracion).getTime() - Date.now();
      if (diff <= 0) {
        setExpired(true);
        setDisplay('00:00');
        if (!firedRef.current) {
          firedRef.current = true;
          cbRef.current?.();
        }
        return;
      }
      const mins = Math.floor(diff / 60000);
      const secs = Math.floor((diff % 60000) / 1000);
      setDisplay(`${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`);
      setUrgent(diff < 3 * 60000);
    };
    tick();
    const iv = setInterval(tick, 1000);
    return () => clearInterval(iv);
  }, [fechaExpiracion]);

  return (
    <div className={`entradas-countdown ${urgent ? 'urgent' : ''} ${expired ? 'expired' : ''}`}>
      <Icon name="cal" size={13} />
      {expired ? 'Expirado' : display}
    </div>
  );
};

// ─── Skeleton card ────────────────────────────────────────────────────────────
const SkeletonCard = () => (
  <div className="entradas-card entradas-card-skel" aria-hidden="true">
    <div className="skel-line skel-pulse" style={{ width: '60%', height: 18, borderRadius: 6, marginBottom: 12 }} />
    <div className="skel-line skel-pulse" style={{ width: '40%', height: 14, borderRadius: 4, marginBottom: 8 }} />
    <div className="skel-line skel-pulse" style={{ width: '80%', height: 14, borderRadius: 4, marginBottom: 24 }} />
    <div style={{ display: 'flex', gap: 8 }}>
      <div className="skel-line skel-pulse" style={{ flex: 1, height: 38, borderRadius: 999 }} />
      <div className="skel-line skel-pulse" style={{ width: 90, height: 38, borderRadius: 999 }} />
    </div>
  </div>
);

// ─── Empty state ──────────────────────────────────────────────────────────────
const EmptyState = ({ icon = 'ticket', title, sub, action }) => (
  <div className="entradas-empty">
    <div className="entradas-empty-icon"><Icon name={icon} size={40} /></div>
    <p className="entradas-empty-title">{title}</p>
    <p className="entradas-empty-sub">{sub}</p>
    {action}
  </div>
);

// ─── Auth required ────────────────────────────────────────────────────────────
const AuthRequired = ({ onLogin, msg }) => (
  <div className="entradas-auth-required">
    <div className="entradas-auth-icon"><Icon name="shield" size={36} /></div>
    <p className="entradas-auth-title">Acceso restringido</p>
    <p className="entradas-auth-sub">{msg || 'Inicia sesión para acceder a esta sección.'}</p>
    <button className="btn btn-primary btn-lg" onClick={onLogin}>Iniciar sesión</button>
  </div>
);

// ─── Filtros explorar ─────────────────────────────────────────────────────────
const EntradasFilterBar = ({ filters, setFilters, availableCities, availablePhases, totalCount, filteredCount }) => {
  const set = (k) => (e) => setFilters((f) => ({ ...f, [k]: e.target.value }));
  const activeCount = Object.values(filters).filter(Boolean).length;
  const reset = () => setFilters({ search: '', pais: '', ciudad: '', fase: '', precioOrder: '', dispMin: '' });

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
            placeholder="México, España, Brasil…"
            value={filters.search}
            onChange={set('search')}
          />
          {filters.search && (
            <button className="cal-search-clear" onClick={() => setFilters((f) => ({ ...f, search: '' }))}>×</button>
          )}
        </div>
      </div>

      <div className="cal-filter-group">
        <label className="cal-filter-label"><Icon name="pin" size={11} /> País sede</label>
        <div className="cal-country-btns">
          {[{ v: '', l: 'Todos' }, { v: 'USA', l: '🇺🇸 USA' }, { v: 'MEX', l: '🇲🇽 México' }, { v: 'CAN', l: '🇨🇦 Canadá' }].map((o) => (
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

      {availableCities.length > 0 && (
        <div className="cal-filter-group">
          <label className="cal-filter-label"><Icon name="stadium" size={11} /> Ciudad</label>
          <select className="cal-filter-select" value={filters.ciudad} onChange={set('ciudad')}>
            <option value="">Todas las ciudades</option>
            {availableCities.map((c) => (
              <option key={c.value} value={c.value}>{c.label}</option>
            ))}
          </select>
        </div>
      )}

      {availablePhases.length > 0 && (
        <div className="cal-filter-group">
          <label className="cal-filter-label"><Icon name="trophy" size={11} /> Fase</label>
          <select className="cal-filter-select" value={filters.fase} onChange={set('fase')}>
            <option value="">Todas las fases</option>
            {availablePhases.map((p) => (
              <option key={p.value} value={p.value}>{p.label}</option>
            ))}
          </select>
        </div>
      )}

      <div className="cal-filter-group">
        <label className="cal-filter-label"><Icon name="ticket" size={11} /> Precio</label>
        <select className="cal-filter-select" value={filters.precioOrder} onChange={set('precioOrder')}>
          <option value="">Sin orden</option>
          <option value="asc">Más barato primero</option>
          <option value="desc">Más caro primero</option>
        </select>
      </div>

      <div className="cal-filter-group">
        <label className="cal-filter-label"><Icon name="users" size={11} /> Disponibilidad mínima</label>
        <select className="cal-filter-select" value={filters.dispMin} onChange={set('dispMin')}>
          <option value="">Cualquiera</option>
          <option value="1">1+ entrada</option>
          <option value="3">3+ entradas</option>
          <option value="5">5+ entradas</option>
          <option value="10">10+ entradas</option>
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

// ─── Partido card (explorar) ──────────────────────────────────────────────────
const PartidoCard = ({ partido, disponibles, onReservar }) => {
  const count = disponibles.length;
  const precioMin = count > 0
    ? Math.min(...disponibles.map(e => e.precio || 0))
    : null;

  return (
    <article className="entradas-partido-card">
      <div className="entradas-partido-card-head">
        <span className="entradas-fase-chip">{partido.fase || 'Grupo'}</span>
        {count > 0
          ? <span className="entradas-disponibles-chip">{count} disponible{count !== 1 ? 's' : ''}</span>
          : <span className="entradas-agotadas-chip">Agotadas</span>
        }
      </div>

      <div className="entradas-partido-match">
        <div className="entradas-team-cell">
          <TeamFlag iso2={teamIso2(partido.equipoLocal)} size="card" />
          <span className="entradas-team-cell-name">{partido.equipoLocal || '?'}</span>
        </div>
        <span className="entradas-vs">VS</span>
        <div className="entradas-team-cell">
          <TeamFlag iso2={teamIso2(partido.equipoVisitante)} size="card" />
          <span className="entradas-team-cell-name">{partido.equipoVisitante || '?'}</span>
        </div>
      </div>

      <div className="entradas-partido-meta">
        <span className="entradas-meta-item">
          <Icon name="cal" size={12} />
          {fmtDateTime(partido.fechaHora)}
        </span>
        <span className="entradas-meta-item">
          <Icon name="stadium" size={12} />
          {partido.sede?.nombreEstadio}
        </span>
        <span className="entradas-meta-item">
          <Icon name="pin" size={12} />
          {partido.sede?.ciudad}
        </span>
      </div>

      {precioMin != null && (
        <div className="entradas-precio-desde">
          Desde <strong>{fmtMoney(precioMin)}</strong>
        </div>
      )}

      <div className="entradas-partido-card-footer">
        <button
          className="entradas-btn-primary"
          onClick={() => onReservar(partido)}
          disabled={count === 0}
        >
          {count > 0 ? 'Ver entradas' : 'Sin disponibilidad'}
          {count > 0 && <Icon name="arrow-r" size={14} />}
        </button>
      </div>
    </article>
  );
};

// ─── Entrada card (mis entradas / historial) ──────────────────────────────────
const EntradaCard = ({
  entrada, partido, factura, reembolso,
  onPagar, onDescargar, onEnviarCorreo, onTransferir, onReembolso,
  loadingPdf, enviandoCorreo,
}) => {
  const isPagada    = entrada.estado === 'PAGADA';
  const isReservada = entrada.estado === 'RESERVADA';
  const puedeOperar = isPagada && !reembolso;

  return (
    <article className="entradas-card">
      <div className="entradas-card-head">
        <EstadoBadge estado={entrada.estado} />
        {entrada.tribuna && (
          <span className="entradas-tribuna-chip">
            <Icon name="stadium" size={11} />
            {capitalize(entrada.tribuna)}
          </span>
        )}
      </div>

      {partido ? (
        <div className="entradas-card-match">
          <span className="entradas-card-team">{partido.equipoLocal}</span>
          <span className="entradas-card-vs">vs</span>
          <span className="entradas-card-team">{partido.equipoVisitante}</span>
        </div>
      ) : (
        <div className="entradas-card-match">
          <span className="entradas-card-team" style={{ color: 'var(--ink-3)' }}>Partido #{entrada.idPartido}</span>
        </div>
      )}

      <div className="entradas-card-meta">
        {partido && (
          <>
            <span><Icon name="cal" size={11} />{fmtDate(partido.fechaHora)}</span>
            <span><Icon name="pin" size={11} />{partido.sede?.nombreEstadio}</span>
          </>
        )}
        {entrada.precio != null && (
          <span><strong>{fmtMoney(entrada.precio)}</strong></span>
        )}
      </div>

      {isReservada && entrada.fechaExpiracionReserva && (
        <div className="entradas-card-countdown-row">
          <span className="entradas-countdown-label">Tiempo para pagar:</span>
          <CountdownTimer fechaExpiracion={entrada.fechaExpiracionReserva} />
        </div>
      )}

      {reembolso && (
        <div className={`entradas-reembolso-badge ${reembolso.estado?.toLowerCase()}`}>
          <Icon name="info" size={11} />
          Reembolso {reembolso.estado === 'PENDIENTE' ? 'pendiente de revisión'
            : reembolso.estado === 'APROBADO' ? 'aprobado'
            : 'rechazado'}
        </div>
      )}

      {entrada.idCorrelacion && (
        <div className="entradas-ref-line">
          Ref: <span className="mono">{entrada.idCorrelacion.slice(0, 13)}…</span>
        </div>
      )}

      <div className="entradas-card-footer">
        {isReservada && (
          <button className="entradas-btn-primary" onClick={() => onPagar(entrada)}>
            <Icon name="check" size={13} />
            Pagar ahora
          </button>
        )}

        {isPagada && (
          <>
            <button
              className="entradas-btn-secondary"
              onClick={() => onDescargar(entrada, factura)}
              disabled={loadingPdf}
            >
              <Icon name="arrow-ur" size={13} />
              {loadingPdf ? 'Descargando…' : 'Factura PDF'}
            </button>
            <button
              className={`entradas-btn-ghost ${factura?.enviadaCorreo ? 'sent' : ''}`}
              onClick={() => onEnviarCorreo(entrada, factura)}
              disabled={enviandoCorreo}
              title={factura?.enviadaCorreo ? 'Reenviar factura al correo' : 'Enviar factura al correo'}
            >
              <Icon name="bell" size={13} />
              {enviandoCorreo ? 'Enviando…' : factura?.enviadaCorreo ? 'Reenviar' : 'Enviar'}
            </button>
          </>
        )}

        {puedeOperar && (
          <div className="entradas-card-footer-extra">
            <button className="entradas-btn-transfer" onClick={() => onTransferir(entrada)}>
              <Icon name="users" size={12} />
              Transferir
            </button>
            <button className="entradas-btn-refund" onClick={() => onReembolso(entrada)}>
              <Icon name="arrow-left" size={12} />
              Reembolso
            </button>
          </div>
        )}
      </div>
    </article>
  );
};

// ─── Reserva card (reservas activas) ──────────────────────────────────────────
const ReservaCard = ({ entrada, partido, onPagar, onExpired }) => (
  <article className="entradas-reserva-card">
    <div className="entradas-reserva-card-left">
      <div className="entradas-reserva-card-match">
        {partido
          ? <>{partido.equipoLocal} <span>vs</span> {partido.equipoVisitante}</>
          : `Partido #${entrada.idPartido}`}
      </div>
      {partido && (
        <div className="entradas-reserva-card-meta">
          <Icon name="pin" size={11} />
          {partido.sede?.nombreEstadio} · {partido.sede?.ciudad}
        </div>
      )}
      {entrada.tribuna && (
        <div className="entradas-reserva-card-meta" style={{ marginTop: 4 }}>
          <Icon name="stadium" size={11} />
          Tribuna: {capitalize(entrada.tribuna)}
          {entrada.precio != null && <> · {fmtMoney(entrada.precio)}</>}
        </div>
      )}
    </div>
    <div className="entradas-reserva-card-right">
      <CountdownTimer fechaExpiracion={entrada.fechaExpiracionReserva} onExpired={() => onExpired(entrada)} />
      <button className="entradas-btn-primary" onClick={() => onPagar(entrada)}>
        Pagar ahora
      </button>
    </div>
  </article>
);

// ─── Reservar modal ───────────────────────────────────────────────────────────
const ReservarModal = ({ partido, entradas, onClose, onReservar, loading }) => {
  const [selected, setSelected] = useState(null);

  useEffect(() => {
    const h = (e) => e.key === 'Escape' && onClose();
    document.addEventListener('keydown', h);
    document.body.style.overflow = 'hidden';
    return () => { document.removeEventListener('keydown', h); document.body.style.overflow = ''; };
  }, [onClose]);

  const disponibles = entradas.filter(e => e.estado === 'DISPONIBLE');

  const groupedByTribuna = disponibles.reduce((acc, e) => {
    const key = e.tribuna || 'General';
    if (!acc[key]) acc[key] = [];
    acc[key].push(e);
    return acc;
  }, {});

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal entradas-modal" onClick={e => e.stopPropagation()}>
        <button className="modal-close" onClick={onClose} aria-label="Cerrar">
          <Icon name="close" size={18} />
        </button>

        <div className="entradas-modal-head">
          <div className="entradas-modal-icon"><Icon name="ticket" size={26} /></div>
          <div>
            <h2 className="entradas-modal-title">
              {partido.equipoLocal} vs {partido.equipoVisitante}
            </h2>
            <p className="entradas-modal-sub">
              <Icon name="cal" size={12} /> {fmtDateTime(partido.fechaHora)}
              &nbsp;·&nbsp;
              <Icon name="pin" size={12} /> {partido.sede?.nombreEstadio}, {partido.sede?.ciudad}
            </p>
          </div>
        </div>

        <p className="entradas-modal-section-label">Selecciona tu entrada</p>

        {disponibles.length === 0 ? (
          <div className="entradas-modal-empty">No hay entradas disponibles para este partido.</div>
        ) : (
          <div className="entradas-tribuna-list">
            {Object.entries(groupedByTribuna).map(([tribuna, items]) => (
              <div key={tribuna} className="entradas-tribuna-group">
                <div className="entradas-tribuna-group-label">
                  <Icon name="stadium" size={12} /> {capitalize(tribuna)}
                  <span className="entradas-tribuna-count">{items.length} disponible{items.length !== 1 ? 's' : ''}</span>
                </div>
                <div className="entradas-tribuna-options">
                  {items.map(e => (
                    <button
                      key={e.id}
                      className={`entradas-tribuna-option ${selected?.id === e.id ? 'selected' : ''}`}
                      onClick={() => setSelected(e)}
                    >
                      <span className="entradas-tribuna-option-label">{capitalize(tribuna)}</span>
                      <span className="entradas-tribuna-option-price">{fmtMoney(e.precio)}</span>
                    </button>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}

        {selected && (
          <div className="entradas-modal-summary">
            <div className="entradas-modal-summary-row">
              <span>Tribuna</span>
              <strong>{capitalize(selected.tribuna || 'General')}</strong>
            </div>
            <div className="entradas-modal-summary-row">
              <span>Precio</span>
              <strong>{fmtMoney(selected.precio)}</strong>
            </div>
            <div className="entradas-modal-summary-note">
              <Icon name="info" size={12} />
              Tienes 15 minutos para confirmar el pago una vez reservada.
            </div>
          </div>
        )}

        <button
          className="modal-btn"
          onClick={() => selected && onReservar(selected)}
          disabled={!selected || loading}
        >
          {loading ? 'Reservando…' : 'Reservar entrada'}
        </button>
      </div>
    </div>
  );
};

// ─── Pago modal — simulación real de compra con validación de tarjeta ────────
const PagoModal = ({ entrada, partido, onClose, onConfirmar, loading, success }) => {
  const [cardNum,  setCardNum]  = useState('');
  const [cardName, setCardName] = useState('');
  const [expiry,   setExpiry]   = useState('');
  const [cvv,      setCvv]      = useState('');
  const [showCvv,  setShowCvv]  = useState(false);
  const [errors,   setErrors]   = useState({});
  const [expired,  setExpired]  = useState(false);

  const cardType  = detectCard(cardNum);
  const cardMeta  = cardType ? CARD_META[cardType] : null;
  const cvvMax    = cardMeta?.cvvLen || 3;
  const rawDigits = cardNum.replace(/\s/g, '');
  const cardOk    = rawDigits.length === 16 && luhn(rawDigits);
  const expiryOk  = validExpiry(expiry);
  const cvvOk     = cvv.length === cvvMax;
  const nameOk    = cardName.trim().split(/\s+/).length >= 2;
  const canPay    = cardOk && expiryOk && cvvOk && nameOk && !expired && !loading;

  useEffect(() => {
    const h = (e) => e.key === 'Escape' && !loading && onClose();
    document.addEventListener('keydown', h);
    document.body.style.overflow = 'hidden';
    return () => { document.removeEventListener('keydown', h); document.body.style.overflow = ''; };
  }, [onClose, loading]);

  const validate = () => {
    const errs = {};
    if (!cardOk)   errs.card   = rawDigits.length < 16 ? 'Número incompleto.' : 'Número de tarjeta inválido.';
    if (!expiryOk) errs.expiry = 'Fecha inválida o tarjeta vencida.';
    if (!cvvOk)    errs.cvv    = `El CVV debe tener ${cvvMax} dígitos.`;
    if (!nameOk)   errs.name   = 'Ingresa nombre y apellido como aparecen en la tarjeta.';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handlePay = () => { if (validate()) onConfirmar(genTxnId()); };

  if (success) {
    return (
      <div className="modal-overlay" onClick={onClose}>
        <div className="modal entradas-modal" onClick={e => e.stopPropagation()}>
          <div className="entradas-pago-success">
            <div className="entradas-pago-success-icon"><Icon name="check" size={36} /></div>
            <h2>¡Pago confirmado!</h2>
            <p>Tu entrada ha sido confirmada exitosamente.</p>
            <p className="entradas-pago-success-sub">La factura fue generada y enviada a tu correo automáticamente.</p>
            <div className="entradas-pago-success-actions">
              <button className="modal-btn" onClick={onClose}>Ver mis entradas</button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="modal-overlay" onClick={() => !loading && onClose()}>
      <div className="modal entradas-modal entradas-pago-modal" onClick={e => e.stopPropagation()}>
        {!loading && <button className="modal-close" onClick={onClose}><Icon name="close" size={18} /></button>}

        <div className="entradas-modal-head">
          <div className="entradas-modal-icon pago"><Icon name="check" size={24} /></div>
          <div>
            <h2 className="entradas-modal-title">Confirmar compra</h2>
            <p className="entradas-modal-sub">
              {partido ? `${partido.equipoLocal} vs ${partido.equipoVisitante}` : `Entrada #${entrada.id}`}
              {entrada.tribuna && <> · {capitalize(entrada.tribuna)}</>}
            </p>
          </div>
        </div>

        {/* Countdown del TTL */}
        {entrada.fechaExpiracionReserva && (
          <div className="entradas-pago-ttl-bar">
            <span>Tiempo para confirmar:</span>
            <CountdownTimer fechaExpiracion={entrada.fechaExpiracionReserva} onExpired={() => setExpired(true)} />
          </div>
        )}

        {expired ? (
          <div className="entradas-pago-expired-msg">
            <Icon name="info" size={14} />
            La reserva ha expirado. Cierra y vuelve a reservar.
          </div>
        ) : (
          <>
            {/* Resumen de cobro */}
            <div className="entradas-pago-summary">
              <div className="entradas-pago-summary-row">
                <span>Tribuna {capitalize(entrada.tribuna || 'General')}</span>
                <span>{fmtMoney(entrada.precio)}</span>
              </div>
              <div className="entradas-pago-summary-row total">
                <strong>Total</strong>
                <strong className="entradas-pago-total">{fmtMoney(entrada.precio)}</strong>
              </div>
            </div>

            {/* Visual de tarjeta */}
            <div className={`entradas-card-visual ${cardType || 'blank'}`}>
              <div className="entradas-card-visual-top">
                <div className="entradas-card-visual-chip" />
                {cardType && <span className="entradas-card-visual-type">{cardMeta?.label}</span>}
              </div>
              <div className="entradas-card-visual-number">
                {cardNum || '•••• •••• •••• ••••'}
              </div>
              <div className="entradas-card-visual-bottom">
                <div>
                  <div className="entradas-card-visual-label">TITULAR</div>
                  <div className="entradas-card-visual-name">{cardName.trim().toUpperCase() || 'NOMBRE APELLIDO'}</div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div className="entradas-card-visual-label">VENCE</div>
                  <div className="entradas-card-visual-expiry">{expiry || 'MM/AA'}</div>
                </div>
              </div>
            </div>

            <div className="entradas-pago-sandbox-badge">
              <Icon name="shield" size={11} />
              Entorno sandbox — ningún cargo real será efectuado
            </div>

            {/* Formulario real */}
            <div className="entradas-pago-form">
              {/* Número de tarjeta */}
              <div className="input-group">
                <label className="input-label">Número de tarjeta</label>
                <div className={`entradas-card-input-wrap ${errors.card ? 'error' : cardOk ? 'valid' : ''}`}>
                  <input
                    className="entradas-card-input"
                    placeholder="0000 0000 0000 0000"
                    value={cardNum}
                    maxLength={19}
                    inputMode="numeric"
                    autoComplete="cc-number"
                    onChange={e => { setCardNum(fmtCardNum(e.target.value)); setErrors(p => ({ ...p, card: undefined })); }}
                  />
                  {cardOk && <span className="entradas-card-input-ok"><Icon name="check" size={13} /></span>}
                </div>
                {errors.card && <span className="entradas-field-error">{errors.card}</span>}
              </div>

              {/* Nombre del titular */}
              <div className="input-group">
                <label className="input-label">Nombre del titular</label>
                <input
                  className={`input ${errors.name ? 'error' : ''}`}
                  placeholder="Como aparece en la tarjeta"
                  value={cardName}
                  autoComplete="cc-name"
                  onChange={e => { setCardName(e.target.value.toUpperCase()); setErrors(p => ({ ...p, name: undefined })); }}
                />
                {errors.name && <span className="entradas-field-error">{errors.name}</span>}
              </div>

              {/* Vencimiento + CVV */}
              <div className="entradas-pago-form-row">
                <div className="input-group">
                  <label className="input-label">Vencimiento</label>
                  <input
                    className={`input ${errors.expiry ? 'error' : expiryOk ? 'valid' : ''}`}
                    placeholder="MM/AA"
                    value={expiry}
                    maxLength={5}
                    inputMode="numeric"
                    autoComplete="cc-exp"
                    onChange={e => { setExpiry(fmtExpiry(e.target.value)); setErrors(p => ({ ...p, expiry: undefined })); }}
                  />
                  {errors.expiry && <span className="entradas-field-error">{errors.expiry}</span>}
                </div>
                <div className="input-group">
                  <label className="input-label">CVV {cardType === 'amex' ? '(4 díg.)' : '(3 díg.)'}</label>
                  <div className={`entradas-card-input-wrap ${errors.cvv ? 'error' : cvvOk ? 'valid' : ''}`}>
                    <input
                      className="entradas-card-input"
                      placeholder={cardType === 'amex' ? '0000' : '000'}
                      value={cvv}
                      maxLength={cvvMax}
                      inputMode="numeric"
                      autoComplete="cc-csc"
                      type={showCvv ? 'text' : 'password'}
                      onChange={e => { setCvv(e.target.value.replace(/\D/g, '').slice(0, cvvMax)); setErrors(p => ({ ...p, cvv: undefined })); }}
                    />
                    <button type="button" className="entradas-cvv-toggle" onClick={() => setShowCvv(s => !s)} tabIndex={-1}>
                      <Icon name={showCvv ? 'eye-off' : 'eye'} size={14} />
                    </button>
                  </div>
                  {errors.cvv && <span className="entradas-field-error">{errors.cvv}</span>}
                </div>
              </div>
            </div>

            <button className="modal-btn" onClick={handlePay} disabled={loading}>
              {loading ? 'Procesando pago…' : `Pagar ${fmtMoney(entrada.precio)}`}
            </button>

            {!canPay && !loading && (
              <p className="entradas-pago-hint">
                <Icon name="info" size={12} />
                Completa todos los campos correctamente para habilitar el pago.
              </p>
            )}
          </>
        )}
      </div>
    </div>
  );
};

// ─── Transferir modal ─────────────────────────────────────────────────────────
const TransferirModal = ({ entrada, partido, userId, onClose, onTransferir, loading }) => {
  const [destinoId,   setDestinoId]   = useState('');
  const [destinoUser, setDestinoUser] = useState(null);
  const [buscando,    setBuscando]    = useState(false);
  const [buscaError,  setBuscaError]  = useState('');
  const [confirmed,   setConfirmed]   = useState(false);

  useEffect(() => {
    const h = (e) => e.key === 'Escape' && !loading && onClose();
    document.addEventListener('keydown', h);
    document.body.style.overflow = 'hidden';
    return () => { document.removeEventListener('keydown', h); document.body.style.overflow = ''; };
  }, [onClose, loading]);

  const handleBuscar = async () => {
    const id = parseInt(destinoId, 10);
    if (!id || isNaN(id)) { setBuscaError('Ingresa un ID válido.'); return; }
    if (id === userId)     { setBuscaError('No puedes transferirte a ti mismo.'); return; }
    setBuscando(true); setBuscaError(''); setDestinoUser(null);
    try {
      const res = await getUsuarioById(id);
      if (!res.data) { setBuscaError('Usuario no encontrado.'); return; }
      setDestinoUser(res.data);
      setConfirmed(false);
    } catch {
      setBuscaError('Usuario no encontrado.');
    } finally {
      setBuscando(false);
    }
  };

  const nombreDestino = destinoUser
    ? [capitalize(destinoUser.nombres), capitalize(destinoUser.apellidos)].filter(Boolean).join(' ')
    : '';

  return (
    <div className="modal-overlay" onClick={() => !loading && onClose()}>
      <div className="modal entradas-modal" onClick={e => e.stopPropagation()}>
        {!loading && <button className="modal-close" onClick={onClose}><Icon name="close" size={18} /></button>}

        <div className="entradas-modal-head">
          <div className="entradas-modal-icon" style={{ background: 'rgba(139,92,246,.12)', color: '#7c3aed' }}>
            <Icon name="users" size={24} />
          </div>
          <div>
            <h2 className="entradas-modal-title">Transferir entrada</h2>
            <p className="entradas-modal-sub">
              {partido ? `${partido.equipoLocal} vs ${partido.equipoVisitante}` : `Entrada #${entrada.id}`}
              {entrada.tribuna && <> · {capitalize(entrada.tribuna)}</>}
            </p>
          </div>
        </div>

        <div className="entradas-transfer-warning">
          <Icon name="info" size={13} />
          Esta acción es <strong>irreversible</strong>. La entrada quedará a nombre del destinatario y ya no podrás usarla.
        </div>

        <div className="input-group">
          <label className="input-label">ID del destinatario</label>
          <div className="entradas-transfer-search-row">
            <input
              className="input"
              placeholder="Ej: 42"
              value={destinoId}
              inputMode="numeric"
              onChange={e => { setDestinoId(e.target.value.replace(/\D/g, '')); setDestinoUser(null); setBuscaError(''); setConfirmed(false); }}
              onKeyDown={e => e.key === 'Enter' && handleBuscar()}
            />
            <button className="entradas-btn-secondary" onClick={handleBuscar} disabled={buscando || !destinoId} style={{ flexShrink: 0 }}>
              {buscando ? '…' : 'Buscar'}
            </button>
          </div>
          {buscaError && <span className="entradas-field-error">{buscaError}</span>}
          <p className="entradas-transfer-id-hint">
            <Icon name="info" size={11} />
            El destinatario puede ver su ID en Mi cuenta &rarr; Perfil.
          </p>
        </div>

        {destinoUser && (
          <>
            <div className="entradas-transfer-user-found">
              <div className="entradas-transfer-avatar">
                {capitalize(destinoUser.nombres)?.[0] || '?'}
              </div>
              <div style={{ flex: 1 }}>
                <div className="entradas-transfer-user-name">{nombreDestino}</div>
                <div className="entradas-transfer-user-email">{destinoUser.correoUsuario}</div>
              </div>
              <Icon name="check" size={16} />
            </div>

            <label className="entradas-transfer-confirm-row">
              <input type="checkbox" checked={confirmed} onChange={e => setConfirmed(e.target.checked)} />
              <span>
                Entiendo que esta transferencia es <strong>permanente e irreversible</strong>.
                La entrada pasará a ser propiedad de <strong>{nombreDestino}</strong>.
              </span>
            </label>

            <button className="modal-btn" onClick={() => confirmed && onTransferir(destinoUser.id)} disabled={!confirmed || loading}>
              {loading ? 'Transfiriendo…' : 'Confirmar transferencia'}
            </button>
          </>
        )}
      </div>
    </div>
  );
};

// ─── Reembolso modal ──────────────────────────────────────────────────────────
const ReembolsoModal = ({ entrada, partido, onClose, onReembolso, loading }) => {
  const [motivo,    setMotivo]    = useState('');
  const [confirmed, setConfirmed] = useState(false);
  const MIN = 20;
  const count = motivo.trim().length;
  const canSubmit = count >= MIN && confirmed && !loading;

  useEffect(() => {
    const h = (e) => e.key === 'Escape' && !loading && onClose();
    document.addEventListener('keydown', h);
    document.body.style.overflow = 'hidden';
    return () => { document.removeEventListener('keydown', h); document.body.style.overflow = ''; };
  }, [onClose, loading]);

  return (
    <div className="modal-overlay" onClick={() => !loading && onClose()}>
      <div className="modal entradas-modal" onClick={e => e.stopPropagation()}>
        {!loading && <button className="modal-close" onClick={onClose}><Icon name="close" size={18} /></button>}

        <div className="entradas-modal-head">
          <div className="entradas-modal-icon" style={{ background: 'rgba(255,59,59,.1)', color: 'var(--live)' }}>
            <Icon name="arrow-left" size={24} />
          </div>
          <div>
            <h2 className="entradas-modal-title">Solicitar reembolso</h2>
            <p className="entradas-modal-sub">
              {partido ? `${partido.equipoLocal} vs ${partido.equipoVisitante}` : `Entrada #${entrada.id}`}
              {entrada.precio != null && <> · {fmtMoney(entrada.precio)}</>}
            </p>
          </div>
        </div>

        <div className="entradas-reembolso-warning">
          <Icon name="info" size={13} />
          <span>
            El reembolso queda en estado <strong>PENDIENTE</strong> hasta ser aprobado por un administrador.
            El proceso puede tardar varios días hábiles.
          </span>
        </div>

        <div className="input-group">
          <label className="input-label" style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span>Motivo del reembolso</span>
            <span className={`entradas-char-count ${count < MIN ? 'warn' : 'ok'}`}>{count}/{MIN} mín.</span>
          </label>
          <textarea
            className="input pref-textarea"
            placeholder="Describe el motivo por el que solicitas el reembolso…"
            value={motivo}
            onChange={e => setMotivo(e.target.value)}
            rows={4}
            maxLength={500}
          />
        </div>

        <label className="entradas-transfer-confirm-row">
          <input type="checkbox" checked={confirmed} onChange={e => setConfirmed(e.target.checked)} />
          <span>
            Entiendo que el reembolso está sujeto a revisión y aprobación, y que una vez aprobado
            el monto se devuelve al método de pago original.
          </span>
        </label>

        <button
          className="modal-btn"
          style={{ background: canSubmit ? 'var(--live)' : undefined }}
          onClick={() => canSubmit && onReembolso(motivo.trim())}
          disabled={!canSubmit}
        >
          {loading ? 'Enviando solicitud…' : 'Solicitar reembolso'}
        </button>
      </div>
    </div>
  );
};

// ─── Main page ────────────────────────────────────────────────────────────────
const Entradas = () => {
  const { dark, toggleDark } = useDarkMode();
  const {
    loggedIn, user, modal, setToast,
    openLogin, openRegister, closeModal,
    handleLoginSuccess, handleRegisterSuccess, handleLogout,
  } = useAuth();

  const userId = user?.usuario?.id || user?.id;

  // ── Data ───────────────────────────────────────────────────────────────────
  const [partidos, setPartidos]               = useState([]);
  const [entradasDisponibles, setEntradasDisp] = useState([]);
  const [misEntradas, setMisEntradas]         = useState([]);
  const [facturasMap, setFacturasMap]         = useState({});

  // ── Loading ────────────────────────────────────────────────────────────────
  const [loadingPartidos, setLoadingPartidos]     = useState(true);
  const [loadingMisEntradas, setLoadingMisEntradas] = useState(false);
  const [loadingReservar, setLoadingReservar]     = useState(false);
  const [loadingPago, setLoadingPago]             = useState(false);
  const [pagoSuccess, setPagoSuccess]             = useState(false);
  const [loadingPdfMap, setLoadingPdfMap]         = useState({});
  const [enviandoCorreoMap, setEnviandoCorreoMap] = useState({});
  const [reembolsosMap, setReembolsosMap]               = useState({});
  const [transferirOpen, setTransferirOpen]               = useState(false);
  const [reembolsoOpen, setReembolsoOpen]                 = useState(false);
  const [entradaParaTransferir, setEntradaParaTransferir] = useState(null);
  const [entradaParaReembolso, setEntradaParaReembolso]   = useState(null);
  const [loadingTransferir, setLoadingTransferir]         = useState(false);
  const [loadingReembolso, setLoadingReembolso]           = useState(false);

  // ── UI ─────────────────────────────────────────────────────────────────────
  const [tab, setTab]                       = useState('explorar');
  const [explorarFilters, setExplorarFilters] = useState({ search: '', pais: '', ciudad: '', fase: '', precioOrder: '', dispMin: '' });
  const [reservarOpen, setReservarOpen]     = useState(false);
  const [pagoOpen, setPagoOpen]             = useState(false);
  const [selectedPartido, setSelectedPartido] = useState(null);
  const [entradasModal, setEntradasModal]   = useState([]);
  const [entradaParaPagar, setEntradaParaPagar] = useState(null);

  // ── Derived: partidos map ──────────────────────────────────────────────────
  const partidosMap = partidos.reduce((acc, p) => { acc[p.id] = p; return acc; }, {});

  // ── Load partidos ──────────────────────────────────────────────────────────
  const loadPartidos = useCallback(() => {
    setLoadingPartidos(true);
    getTodosPartidos()
      .then(r => setPartidos(Array.isArray(r.data) ? r.data : []))
      .catch(() => setPartidos([]))
      .finally(() => setLoadingPartidos(false));
  }, []);

  // ── Load entradas disponibles ──────────────────────────────────────────────
  const loadDisponibles = useCallback(() => {
    getEntradasPorEstado('DISPONIBLE')
      .then(r => setEntradasDisp(Array.isArray(r.data) ? r.data : []))
      .catch(() => setEntradasDisp([]));
  }, []);

  // ── Load mis entradas ──────────────────────────────────────────────────────
  const loadMisEntradas = useCallback(() => {
    if (!loggedIn || !userId) { setMisEntradas([]); return; }
    setLoadingMisEntradas(true);
    getEntradasPorTitular(userId)
      .then(r => setMisEntradas(Array.isArray(r.data) ? r.data : []))
      .catch(() => setMisEntradas([]))
      .finally(() => setLoadingMisEntradas(false));
  }, [loggedIn, userId]);

  // ── Load reembolsos ────────────────────────────────────────────────────────
  const loadReembolsos = useCallback(() => {
    if (!loggedIn || !userId) { setReembolsosMap({}); return; }
    getReembolsosPorUsuario(userId)
      .then(r => {
        const arr = Array.isArray(r.data) ? r.data : [];
        const map = arr.reduce((acc, rb) => { acc[rb.idEntrada] = rb; return acc; }, {});
        setReembolsosMap(map);
      })
      .catch(() => setReembolsosMap({}));
  }, [loggedIn, userId]);

  // ── Load facturas for PAGADA entries ──────────────────────────────────────
  useEffect(() => {
    const pagadas = misEntradas.filter(e => e.estado === 'PAGADA');
    pagadas.forEach(e => {
      if (facturasMap[e.id] !== undefined) return;
      getFacturaPorEntrada(e.id)
        .then(r => {
          setFacturasMap(prev => ({ ...prev, [e.id]: r.data || null }));
        })
        .catch(() => {
          setFacturasMap(prev => ({ ...prev, [e.id]: null }));
        });
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [misEntradas]);

  useEffect(() => { loadPartidos(); loadDisponibles(); }, [loadPartidos, loadDisponibles]);
  useEffect(() => { loadMisEntradas(); }, [loadMisEntradas]);
  useEffect(() => { loadReembolsos(); }, [loadReembolsos]);

  // ── Derived stats ──────────────────────────────────────────────────────────
  const proximos   = partidos.filter(p => p.estado === 'PROGRAMADO');
  const isReservaVigente = (e) => e.estado === 'RESERVADA'
    && (!e.fechaExpiracionReserva || new Date(e.fechaExpiracionReserva).getTime() > Date.now());
  const reservas   = misEntradas.filter(isReservaVigente);
  const confirmadas = misEntradas.filter(e => e.estado === 'PAGADA');

  const statsHero = [
    { num: loadingPartidos ? '…' : entradasDisponibles.length.toString(), lbl: 'Entradas disponibles' },
    { num: loadingPartidos ? '…' : proximos.length.toString(), lbl: 'Próximos partidos' },
    { num: loggedIn ? (loadingMisEntradas ? '…' : reservas.length.toString()) : '–', lbl: 'Mis reservas' },
    { num: loggedIn ? (loadingMisEntradas ? '…' : confirmadas.length.toString()) : '–', lbl: 'Entradas confirmadas' },
  ];

  // ── EXPLORAR: disponibles por partido ─────────────────────────────────────
  const dispPorPartido = entradasDisponibles.reduce((acc, e) => {
    if (!acc[e.idPartido]) acc[e.idPartido] = [];
    acc[e.idPartido].push(e);
    return acc;
  }, {});

  // Mostramos TODOS los próximos partidos; el botón queda deshabilitado si count=0
  const partidosConBoleteria = proximos;

  // ── Filtros explorar ───────────────────────────────────────────────────────
  const availableCities = useMemo(() => {
    const seen = new Set();
    const cities = [];
    partidos.forEach((p) => {
      const city = p.sede?.ciudad;
      if (city && !seen.has(city)) { seen.add(city); cities.push({ value: city, label: city }); }
    });
    return cities.sort((a, b) => a.label.localeCompare(b.label));
  }, [partidos]);

  const filteredCities = useMemo(
    () => !explorarFilters.pais ? availableCities : availableCities.filter((c) => getVenueCountry(c.value) === explorarFilters.pais),
    [availableCities, explorarFilters.pais]
  );

  const availablePhases = useMemo(() => {
    const seen = new Set();
    const phases = [];
    partidos.forEach((p) => {
      if (p.fase && !seen.has(p.fase)) { seen.add(p.fase); phases.push({ value: p.fase, label: adaptPhase(p.fase) }); }
    });
    return phases;
  }, [partidos]);

  const filteredPartidos = useMemo(() => {
    let list = partidosConBoleteria;
    if (explorarFilters.search) {
      const q = explorarFilters.search.toLowerCase();
      list = list.filter((p) =>
        (p.equipoLocal || '').toLowerCase().includes(q) ||
        (p.equipoVisitante || '').toLowerCase().includes(q)
      );
    }
    if (explorarFilters.pais) list = list.filter((p) => getVenueCountry(p.sede?.ciudad) === explorarFilters.pais);
    if (explorarFilters.ciudad) list = list.filter((p) => (p.sede?.ciudad || '') === explorarFilters.ciudad);
    if (explorarFilters.fase) list = list.filter((p) => p.fase === explorarFilters.fase);
    if (explorarFilters.dispMin) {
      const min = parseInt(explorarFilters.dispMin, 10);
      list = list.filter((p) => (dispPorPartido[p.id]?.length || 0) >= min);
    }
    if (explorarFilters.precioOrder) {
      list = [...list].sort((a, b) => {
        const da = dispPorPartido[a.id] || [];
        const db = dispPorPartido[b.id] || [];
        const pa = da.length > 0 ? Math.min(...da.map((e) => e.precio || 0)) : (explorarFilters.precioOrder === 'asc' ? Infinity : -1);
        const pb = db.length > 0 ? Math.min(...db.map((e) => e.precio || 0)) : (explorarFilters.precioOrder === 'asc' ? Infinity : -1);
        return explorarFilters.precioOrder === 'asc' ? pa - pb : pb - pa;
      });
    }
    return list;
  }, [partidosConBoleteria, explorarFilters, dispPorPartido]);

  // ── Actions ────────────────────────────────────────────────────────────────
  const handleVerEntradas = (partido) => {
    if (!loggedIn) { openLogin(); return; }
    setSelectedPartido(partido);
    setEntradasModal(dispPorPartido[partido.id] || []);
    setReservarOpen(true);
  };

  const handleReservar = async (entrada) => {
    if (!loggedIn || !userId) { openLogin(); return; }
    setLoadingReservar(true);
    try {
      await reservarEntrada({
        id: entrada.id,
        idPartido: entrada.idPartido,
        idTitular: userId,
        tribuna: entrada.tribuna,
        precio: entrada.precio,
        estado: entrada.estado,
      });
      setToast('¡Entrada reservada! Tienes 15 minutos para confirmar el pago.');
      setReservarOpen(false);
      loadDisponibles();
      loadMisEntradas();
      setTab('reservas');
    } catch (e) {
      setToast(e.response?.data?.mensaje || 'Error al reservar la entrada.');
    } finally {
      setLoadingReservar(false);
    }
  };

  const handleAbrirPago = (entrada) => {
    setEntradaParaPagar(entrada);
    setPagoSuccess(false);
    setPagoOpen(true);
  };

  const handleConfirmarPago = async (idTransaccion) => {
    if (!entradaParaPagar) return;
    setLoadingPago(true);
    try {
      await confirmarPago(entradaParaPagar.id, idTransaccion);
      setPagoSuccess(true);
      loadMisEntradas();
      loadDisponibles();
    } catch (e) {
      setToast(e.response?.data?.mensaje || 'Error al confirmar el pago.');
      setPagoOpen(false);
    } finally {
      setLoadingPago(false);
    }
  };

  const handleCerrarPago = () => {
    setPagoOpen(false);
    setEntradaParaPagar(null);
    setPagoSuccess(false);
    if (pagoSuccess) setTab('mis-entradas');
  };

  const handleDescargarFactura = async (entrada, factura) => {
    setLoadingPdfMap(prev => ({ ...prev, [entrada.id]: true }));
    try {
      let f = factura;
      if (!f) {
        const res = await getFacturaPorEntrada(entrada.id);
        f = res.data;
        if (f) setFacturasMap(prev => ({ ...prev, [entrada.id]: f }));
      }
      if (!f) { setToast('No se encontró la factura para esta entrada.'); return; }
      await descargarFacturaPdf(f.id, f.numeroFactura);
    } catch {
      setToast('Error al descargar la factura.');
    } finally {
      setLoadingPdfMap(prev => ({ ...prev, [entrada.id]: false }));
    }
  };

  const handleEnviarCorreo = async (entrada, factura) => {
    setEnviandoCorreoMap(prev => ({ ...prev, [entrada.id]: true }));
    try {
      let f = factura;
      if (!f) {
        const res = await getFacturaPorEntrada(entrada.id);
        f = res.data;
        if (f) setFacturasMap(prev => ({ ...prev, [entrada.id]: f }));
      }
      if (!f) { setToast('No se encontró la factura para esta entrada.'); return; }
      await enviarFacturaCorreo(f.id);
      setToast('Factura enviada exitosamente a tu correo.');
      setFacturasMap(prev => ({ ...prev, [entrada.id]: { ...f, enviadaCorreo: 1 } }));
    } catch {
      setToast('Error al enviar la factura por correo.');
    } finally {
      setEnviandoCorreoMap(prev => ({ ...prev, [entrada.id]: false }));
    }
  };

  const handleReservaExpirada = useCallback((entrada) => {
    setMisEntradas(prev =>
      prev.map(e => e.id === entrada.id ? { ...e, estado: 'EXPIRADA' } : e)
    );
  }, []);

  const handleAbrirTransferir = (entrada) => {
    setEntradaParaTransferir(entrada);
    setTransferirOpen(true);
  };

  const handleTransferir = async (idDestino) => {
    if (!entradaParaTransferir || !userId) return;
    setLoadingTransferir(true);
    try {
      const res = await transferirEntrada({
        idEntrada: entradaParaTransferir.id,
        idUsuarioOrigen: userId,
        idUsuarioDestino: idDestino,
      });
      setToast(res?.data?.mensaje || 'Entrada transferida. Se envió un correo a ti y al destinatario.');
      setTransferirOpen(false);
      setEntradaParaTransferir(null);
      loadMisEntradas();
    } catch (e) {
      setToast(e.response?.data?.mensaje || 'Error al transferir la entrada.');
    } finally {
      setLoadingTransferir(false);
    }
  };

  const handleAbrirReembolso = (entrada) => {
    setEntradaParaReembolso(entrada);
    setReembolsoOpen(true);
  };

  const handleReembolso = async (motivo) => {
    if (!entradaParaReembolso || !userId) return;
    setLoadingReembolso(true);
    try {
      await solicitarReembolso({
        idEntrada: entradaParaReembolso.id,
        idUsuario: userId,
        motivo,
      });
      setToast('Solicitud de reembolso enviada. Quedará pendiente de revisión.');
      setReembolsoOpen(false);
      setEntradaParaReembolso(null);
      loadReembolsos();
    } catch (e) {
      setToast(e.response?.data?.mensaje || 'Error al solicitar el reembolso.');
    } finally {
      setLoadingReembolso(false);
    }
  };

  // ── Tabs config ────────────────────────────────────────────────────────────
  const TABS = [
    { id: 'explorar',     label: 'Explorar',     icon: 'search' },
    { id: 'mis-entradas', label: 'Mis entradas', icon: 'ticket',  requiresAuth: true },
    { id: 'reservas',     label: 'Reservas',     icon: 'cal',     requiresAuth: true,
      badge: loggedIn && reservas.length > 0 ? reservas.length : null },
    { id: 'historial',    label: 'Historial',    icon: 'list',    requiresAuth: true },
  ];

  // ── Render ─────────────────────────────────────────────────────────────────
  return (
    <div className="app">
      <Navbar
        dark={dark} onToggleDark={toggleDark}
        loggedIn={loggedIn} user={user}
        onLogin={openLogin} onRegister={openRegister} onLogout={handleLogout}
      />

      <PageHero
        badge="FIFA World Cup 2026™"
        badgeIcon="ticket"
        title={<>ENTRADAS<br />MUNDIAL 2026</>}
        subtitle="Reserva tu lugar y vive la emoción desde el estadio. Pago seguro, factura instantánea."
        stats={statsHero}
        variant="entradas"
        mediaKey="entradas"
      />

      {/* ── Tab navigation ─────────────────────────────────────────── */}
      <div className="entradas-tabs-bar">
        <div className="container entradas-tabs">
          {TABS.map(t => {
            const disabled = t.requiresAuth && !loggedIn;
            return (
              <button
                key={t.id}
                className={`entradas-tab ${tab === t.id ? 'active' : ''} ${disabled ? 'disabled' : ''}`}
                onClick={() => {
                  if (disabled) { openLogin(); return; }
                  setTab(t.id);
                }}
              >
                <Icon name={t.icon} size={14} />
                {t.label}
                {t.badge != null && (
                  <span className="entradas-tab-badge">{t.badge}</span>
                )}
              </button>
            );
          })}
        </div>
      </div>

      {/* ── Tab content ─────────────────────────────────────────────── */}
      <main className="container entradas-main">

        {/* ── EXPLORAR ──────────────────────────────────────────────── */}
        {tab === 'explorar' && (
          <div className="entradas-tab-content">
            <div className="entradas-explorar-layout">
              <EntradasFilterBar
                filters={explorarFilters}
                setFilters={setExplorarFilters}
                availableCities={filteredCities}
                availablePhases={availablePhases}
                totalCount={partidosConBoleteria.length}
                filteredCount={filteredPartidos.length}
              />
              <div>
                {loadingPartidos ? (
                  <div className="entradas-grid">
                    {Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)}
                  </div>
                ) : filteredPartidos.length === 0 ? (
                  <EmptyState
                    icon={partidosConBoleteria.length === 0 ? 'stadium' : 'search'}
                    title={partidosConBoleteria.length === 0 ? 'Sin partidos próximos' : 'Sin resultados'}
                    sub={partidosConBoleteria.length === 0 ? 'No hay partidos programados en este momento.' : 'No hay partidos que coincidan con los filtros aplicados.'}
                    action={partidosConBoleteria.length > 0 ? (
                      <button className="btn btn-ghost" style={{ marginTop: 12, border: '1.5px solid var(--line)' }}
                        onClick={() => setExplorarFilters({ search: '', pais: '', ciudad: '', fase: '', precioOrder: '', dispMin: '' })}>
                        Limpiar filtros
                      </button>
                    ) : null}
                  />
                ) : (
                  <>
                    <div className="entradas-explorar-header">
                      <p className="entradas-explorar-desc">
                        <Icon name="info" size={14} />
                        {filteredPartidos.length} partido{filteredPartidos.length !== 1 ? 's' : ''} encontrado{filteredPartidos.length !== 1 ? 's' : ''}
                        {entradasDisponibles.length > 0
                          ? ` · ${entradasDisponibles.length} entrada${entradasDisponibles.length !== 1 ? 's' : ''} disponible${entradasDisponibles.length !== 1 ? 's' : ''}`
                          : ' · Sin entradas disponibles aún'}
                      </p>
                    </div>
                    <div className="entradas-grid">
                      {filteredPartidos.map((p) => (
                        <PartidoCard
                          key={p.id}
                          partido={p}
                          disponibles={dispPorPartido[p.id] || []}
                          onReservar={handleVerEntradas}
                        />
                      ))}
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>
        )}

        {/* ── MIS ENTRADAS ──────────────────────────────────────────── */}
        {tab === 'mis-entradas' && (
          <div className="entradas-tab-content">
            {!loggedIn ? (
              <AuthRequired onLogin={openLogin} msg="Inicia sesión para ver tus entradas confirmadas." />
            ) : loadingMisEntradas ? (
              <div className="entradas-grid">
                {Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={i} />)}
              </div>
            ) : confirmadas.length === 0 ? (
              <EmptyState
                icon="ticket"
                title="Aún no tienes entradas confirmadas"
                sub="Explora los partidos disponibles y reserva tu entrada."
                action={
                  <button className="btn btn-primary btn-lg" onClick={() => setTab('explorar')}>
                    <Icon name="search" size={14} /> Explorar partidos
                  </button>
                }
              />
            ) : (
              <div className="entradas-grid">
                {confirmadas.map(e => (
                  <EntradaCard
                    key={e.id}
                    entrada={e}
                    partido={partidosMap[e.idPartido]}
                    factura={facturasMap[e.id]}
                    reembolso={reembolsosMap[e.id]}
                    onPagar={handleAbrirPago}
                    onDescargar={handleDescargarFactura}
                    onEnviarCorreo={handleEnviarCorreo}
                    onTransferir={handleAbrirTransferir}
                    onReembolso={handleAbrirReembolso}
                    loadingPdf={!!loadingPdfMap[e.id]}
                    enviandoCorreo={!!enviandoCorreoMap[e.id]}
                  />
                ))}
              </div>
            )}
          </div>
        )}

        {/* ── RESERVAS ──────────────────────────────────────────────── */}
        {tab === 'reservas' && (
          <div className="entradas-tab-content">
            {!loggedIn ? (
              <AuthRequired onLogin={openLogin} msg="Inicia sesión para ver tus reservas activas." />
            ) : loadingMisEntradas ? (
              <div className="entradas-reservas-list">
                {Array.from({ length: 2 }).map((_, i) => (
                  <div key={i} className="entradas-reserva-card entradas-card-skel" aria-hidden="true">
                    <div className="skel-line skel-pulse" style={{ flex: 1, height: 20, borderRadius: 6 }} />
                  </div>
                ))}
              </div>
            ) : reservas.length === 0 ? (
              <EmptyState
                icon="cal"
                title="Sin reservas activas"
                sub="Cuando reserves una entrada aquí aparecerá con el tiempo restante para pagar."
                action={
                  <button className="btn btn-primary btn-lg" onClick={() => setTab('explorar')}>
                    <Icon name="search" size={14} /> Explorar partidos
                  </button>
                }
              />
            ) : (
              <>
                <div className="entradas-reservas-alert">
                  <Icon name="info" size={14} />
                  Confirma el pago antes de que expire tu reserva para asegurar tu entrada.
                </div>
                <div className="entradas-reservas-list">
                  {reservas.map(e => (
                    <ReservaCard
                      key={e.id}
                      entrada={e}
                      partido={partidosMap[e.idPartido]}
                      onPagar={handleAbrirPago}
                      onExpired={handleReservaExpirada}
                    />
                  ))}
                </div>
              </>
            )}
          </div>
        )}

        {/* ── HISTORIAL ─────────────────────────────────────────────── */}
        {tab === 'historial' && (
          <div className="entradas-tab-content">
            {!loggedIn ? (
              <AuthRequired onLogin={openLogin} msg="Inicia sesión para ver tu historial de entradas." />
            ) : loadingMisEntradas ? (
              <div className="entradas-grid">
                {Array.from({ length: 4 }).map((_, i) => <SkeletonCard key={i} />)}
              </div>
            ) : misEntradas.length === 0 ? (
              <EmptyState
                icon="list"
                title="Sin historial de entradas"
                sub="Tu actividad de entradas aparecerá aquí."
              />
            ) : (
              <>
                <div className="entradas-historial-header">
                  <span>{misEntradas.length} entrada{misEntradas.length !== 1 ? 's' : ''} en total</span>
                  <div className="entradas-historial-legend">
                    {Object.entries(ESTADO_META).map(([k, v]) => {
                      const count = misEntradas.filter(e => e.estado === k).length;
                      if (!count) return null;
                      return (
                        <span key={k} className={`entradas-historial-legend-item ${v.cls}`}>
                          <span className="entradas-estado-dot" />
                          {v.label}: {count}
                        </span>
                      );
                    })}
                  </div>
                </div>
                <div className="entradas-grid">
                  {[...misEntradas]
                    .sort((a, b) => new Date(b.fechaReserva || 0) - new Date(a.fechaReserva || 0))
                    .map(e => (
                      <EntradaCard
                        key={e.id}
                        entrada={e}
                        partido={partidosMap[e.idPartido]}
                        factura={facturasMap[e.id]}
                        reembolso={reembolsosMap[e.id]}
                        onPagar={handleAbrirPago}
                        onDescargar={handleDescargarFactura}
                        onEnviarCorreo={handleEnviarCorreo}
                        onTransferir={handleAbrirTransferir}
                        onReembolso={handleAbrirReembolso}
                        loadingPdf={!!loadingPdfMap[e.id]}
                        enviandoCorreo={!!enviandoCorreoMap[e.id]}
                      />
                    ))}
                </div>
              </>
            )}
          </div>
        )}
      </main>

      <Footer />

      {/* ── Modals ──────────────────────────────────────────────────── */}
      {reservarOpen && selectedPartido && (
        <ReservarModal
          partido={selectedPartido}
          entradas={entradasModal}
          onClose={() => setReservarOpen(false)}
          onReservar={handleReservar}
          loading={loadingReservar}
        />
      )}

      {pagoOpen && entradaParaPagar && (
        <PagoModal
          entrada={entradaParaPagar}
          partido={partidosMap[entradaParaPagar.idPartido]}
          onClose={handleCerrarPago}
          onConfirmar={handleConfirmarPago}
          loading={loadingPago}
          success={pagoSuccess}
        />
      )}

      {transferirOpen && entradaParaTransferir && (
        <TransferirModal
          entrada={entradaParaTransferir}
          partido={partidosMap[entradaParaTransferir.idPartido]}
          userId={userId}
          onClose={() => { setTransferirOpen(false); setEntradaParaTransferir(null); }}
          onTransferir={handleTransferir}
          loading={loadingTransferir}
        />
      )}

      {reembolsoOpen && entradaParaReembolso && (
        <ReembolsoModal
          entrada={entradaParaReembolso}
          partido={partidosMap[entradaParaReembolso.idPartido]}
          onClose={() => { setReembolsoOpen(false); setEntradaParaReembolso(null); }}
          onReembolso={handleReembolso}
          loading={loadingReembolso}
        />
      )}

      {modal === 'login'    && <LoginModal    onClose={closeModal} onLoginSuccess={handleLoginSuccess}       onSwitchToRegister={openRegister} />}
      {modal === 'register' && <RegisterModal onClose={closeModal} onRegisterSuccess={handleRegisterSuccess} onSwitchToLogin={openLogin} />}
    </div>
  );
};

export default Entradas;
