import React from 'react';
import { HERO_IMAGES } from './pageHeroMedia';

const LEGENDS = [
  {
    img: HERO_IMAGES.messi,
    name: 'Lionel Messi',
    year: '2022',
    momento: 'La consagración en Qatar',
    tag: 'Argentina',
  },
  {
    img: HERO_IMAGES.baggio,
    name: 'Roberto Baggio',
    year: '1994',
    momento: 'El penal que detuvo a Italia',
    tag: 'Italia',
  },
  {
    img: HERO_IMAGES.andresEscobar,
    name: 'Andrés Escobar',
    year: '1994',
    momento: 'Tragedia y memoria del fútbol',
    tag: 'Colombia',
  },
  {
    img: HERO_IMAGES.messiEliminacion,
    name: 'El otro lado',
    year: '2018',
    momento: 'Cuando un gigante cae',
    tag: 'Mundial',
  },
];

const LegendsShowcase = () => (
  <section className="legends-showcase">
    <div className="container">
      <div className="legends-head">
        <span className="legends-eyebrow">Hall of Fame · Mundial</span>
        <h2 className="legends-title">Leyendas que escribieron la historia</h2>
        <p className="legends-sub">
          Cada Copa del Mundo deja héroes, villanos y momentos eternos. El 2026
          se suma a un linaje que comenzó hace casi un siglo.
        </p>
      </div>
      <div className="legends-grid">
        {LEGENDS.map((l) => (
          <article className="legend-card" key={l.name + l.year}>
            <img
              src={l.img}
              alt={l.name}
              className="legend-card-img"
              loading="lazy"
              decoding="async"
            />
            <div className="legend-card-overlay" />
            <div className="legend-card-content">
              <span className="legend-card-tag">{l.tag} · {l.year}</span>
              <h3 className="legend-card-name">{l.name}</h3>
              <p className="legend-card-moment">{l.momento}</p>
            </div>
          </article>
        ))}
      </div>
    </div>
  </section>
);

export default LegendsShowcase;
