import React from 'react';

/**
 * SectionMediaBand
 * Banda decorativa con imagen de fondo difuminada y overlay azul cobalto.
 * Se usa como divisor cinematográfico entre secciones — nunca como contenido
 * principal. Por defecto es full-width y respeta el tema claro/oscuro.
 *
 * Props:
 *  - image:   ruta absoluta (ya resuelta)
 *  - height:  altura de la banda (px o string CSS). Default 220px.
 *  - position: object-position (ej. 'center 25%')
 *  - eyebrow: texto chip superior (opcional)
 *  - title:   título grande (opcional)
 *  - subtitle: subtítulo (opcional)
 *  - intensity: 'soft' | 'default' | 'strong'
 *  - className: extra clases
 */
const SectionMediaBand = ({
  image,
  height = 220,
  position = 'center 30%',
  eyebrow,
  title,
  subtitle,
  intensity = 'strong',
  className = '',
}) => {
  if (!image) return null;
  const style = { height: typeof height === 'number' ? `${height}px` : height };
  return (
    <section
      className={`section-media-band section-media-band--${intensity} ${className}`}
      style={style}
      aria-hidden={!title && !subtitle ? 'true' : undefined}
    >
      <img
        className="section-media-band-img"
        src={image}
        alt=""
        loading="lazy"
        decoding="async"
        style={{ objectPosition: position }}
      />
      <div className="section-media-band-overlay" />
      {(eyebrow || title || subtitle) && (
        <div className="container section-media-band-inner">
          {eyebrow && <span className="section-media-band-eyebrow">{eyebrow}</span>}
          {title && <h2 className="section-media-band-title">{title}</h2>}
          {subtitle && <p className="section-media-band-sub">{subtitle}</p>}
        </div>
      )}
    </section>
  );
};

export default SectionMediaBand;
