import React from 'react';
import Icon from '../ui/Icon';
import HeroMedia from './HeroMedia';
import { getHeroMedia } from './pageHeroMedia';

const PageHero = ({
  badge,
  badgeIcon,
  title,
  subtitle,
  stats,
  variant = 'default',
  mediaKey,
  media,
  intensity,
}) => {
  const resolvedMedia = media || (mediaKey ? getHeroMedia(mediaKey) : null);
  const heroIntensity = intensity || resolvedMedia?.intensity || 'default';

  return (
    <section
      className={
        'page-hero page-hero--' + variant +
        (resolvedMedia ? ' page-hero--has-media' : '')
      }
    >
      <div className="page-hero-bg" />
      {resolvedMedia && (
        <HeroMedia media={resolvedMedia} intensity={heroIntensity} />
      )}
      <div className="container page-hero-inner">
        {badge && (
          <div className="page-hero-eyebrow">
            <span className="page-hero-chip">
              {badgeIcon && <Icon name={badgeIcon} size={11} />}
              {badge}
            </span>
          </div>
        )}
        <h1 className="page-hero-title">{title}</h1>
        {subtitle && <p className="page-hero-sub">{subtitle}</p>}
        {stats && stats.length > 0 && (
          <div className="page-hero-stats-bar">
            {stats.map(({ num, lbl }, i) => (
              <React.Fragment key={lbl}>
                {i > 0 && <div className="page-hero-stat-div" />}
                <div className="page-hero-stat">
                  <span className="page-hero-stat-num">{num}</span>
                  <span className="page-hero-stat-lbl">{lbl}</span>
                </div>
              </React.Fragment>
            ))}
          </div>
        )}
      </div>
    </section>
  );
};

export default PageHero;
