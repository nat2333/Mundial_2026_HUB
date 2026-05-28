import React, { useEffect, useRef, useState } from 'react';

/**
 * HeroMedia
 * Renders a cinematic video or image background layer for hero sections.
 * Sits behind the brand gradient overlay so contrast and legibility are preserved.
 *
 * Props:
 *  - media: { type: 'video' | 'image', src, poster?, fallback?, playbackRate?, position? }
 *  - intensity: 'soft' | 'default' | 'strong'  (controls overlay darkness)
 *  - className: optional extra class
 */
const HeroMedia = ({ media, intensity = 'default', className = '' }) => {
  const videoRef = useRef(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    setFailed(false);
  }, [media?.src]);

  useEffect(() => {
    const v = videoRef.current;
    if (!v || media?.type !== 'video') return;
    const rate = media.playbackRate ?? 0.85;
    const apply = () => { try { v.playbackRate = rate; } catch (_) {} };
    apply();
    v.addEventListener('loadedmetadata', apply);
    return () => v.removeEventListener('loadedmetadata', apply);
  }, [media]);

  if (!media) return null;

  const showImage = failed || media.type === 'image';
  const imgSrc = failed ? (media.fallback || media.poster) : media.src;
  const objectPosition = media.position || 'center';

  return (
    <div
      className={`hero-media hero-media--${intensity} ${className}`}
      aria-hidden="true"
    >
      {!showImage && (
        <video
          ref={videoRef}
          className="hero-media-video"
          src={media.src}
          poster={media.poster || media.fallback}
          autoPlay
          muted
          loop
          playsInline
          preload="metadata"
          onError={() => setFailed(true)}
          style={{ objectPosition }}
        />
      )}
      {showImage && imgSrc && (
        <img
          className="hero-media-img"
          src={imgSrc}
          alt=""
          loading="lazy"
          decoding="async"
          style={{ objectPosition }}
          onError={(e) => { e.currentTarget.style.display = 'none'; }}
        />
      )}
      <div className="hero-media-overlay" />
      <div className="hero-media-grain" />
    </div>
  );
};

export default HeroMedia;
