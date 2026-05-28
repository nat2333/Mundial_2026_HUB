/**
 * Mapeo central de medios cinematográficos por página.
 * Mantiene la coherencia visual del Mundial 2026 Hub y permite reusar
 * el mismo recurso en múltiples zonas (hero principal + fondos secundarios).
 *
 * Cualquier archivo aquí vive en `public/` y se sirve desde la raíz.
 */

const V = (name) => `${process.env.PUBLIC_URL || ''}/videos/${name}`;
const I = (name) => `${process.env.PUBLIC_URL || ''}/imagenes/${name}`;

export const HERO_VIDEOS = {
  cities:     V('16 Cities - Animation.mp4'),
  teams:      V('48 Teams - Animation.mp4'),
  patchwork:  V("FIFA's Patchwork - Animation.mp4"),
  opening:    V('Opening 1 Year Out.mp4'),
};

export const HERO_IMAGES = {
  pele:           I('pele.webp'),
  messi:          I('messi.webp'),
  zidane:         I('zidane.webp'),
  baggio:         I('baggio.webp'),
  iniesta:        I('iniesta.webp'),
  andresEscobar:  I('andres-escobar.webp'),
  final2018:      I('final-mundial-2018.webp'),
  manoDeDios:     I('mano-de-dios.webp'),
  messiEliminacion: I('messi-eliminacion.webp'),
  copa:           I('copa-del-mundial.webp'),
  sinIdentificar: I('imagen-sin-identificar.webp'),
};

/**
 * Recursos por página de la app.
 * type:        'video' | 'image'
 * src:         ruta principal
 * fallback:    imagen de respaldo si el video falla o si la conexión es lenta
 * poster:      imagen mostrada mientras el video carga (mismo que fallback por defecto)
 * playbackRate: factor de velocidad para video (slow motion < 1)
 * intensity:   'soft' | 'default' | 'strong' — controla el oscurecimiento del overlay
 * position:    object-position para encuadrar imágenes (ej. 'center 30%')
 */
export const HERO_MEDIA = {
  inicio: {
    type: 'video',
    src: HERO_VIDEOS.opening,
    fallback: HERO_IMAGES.copa,
    playbackRate: 0.55,
    intensity: 'default',
  },
  sedes: {
    type: 'video',
    src: HERO_VIDEOS.cities,
    fallback: HERO_IMAGES.copa,
    playbackRate: 0.5,
    intensity: 'default',
  },
  selecciones: {
    type: 'video',
    src: HERO_VIDEOS.teams,
    fallback: HERO_IMAGES.copa,
    playbackRate: 0.5,
    intensity: 'default',
  },
  calendario: {
    type: 'video',
    src: HERO_VIDEOS.patchwork,
    fallback: HERO_IMAGES.copa,
    playbackRate: 0.55,
    intensity: 'default',
  },
  posiciones: {
    type: 'video',
    src: HERO_VIDEOS.patchwork,
    fallback: HERO_IMAGES.copa,
    playbackRate: 0.55,
    intensity: 'strong',
  },
  album: {
    type: 'image',
    src: HERO_IMAGES.pele,
    fallback: HERO_IMAGES.zidane,
    position: 'center 25%',
    intensity: 'strong',
  },
  pollas: {
    type: 'image',
    src: HERO_IMAGES.final2018,
    fallback: HERO_IMAGES.manoDeDios,
    position: 'center 30%',
    intensity: 'strong',
  },
  entradas: {
    type: 'image',
    src: HERO_IMAGES.copa,
    fallback: HERO_IMAGES.copa,
    position: 'center 35%',
    intensity: 'default',
  },
  micuenta: {
    type: 'image',
    src: HERO_IMAGES.iniesta,
    fallback: HERO_IMAGES.zidane,
    position: 'center 20%',
    intensity: 'strong',
  },
};

export const getHeroMedia = (key) => HERO_MEDIA[key] || null;

/**
 * Imágenes que NO viajan en heroes — disponibles para usarse
 * como fondos secundarios discretos dentro de las páginas
 * (banners, divisores, zonas vacías del layout).
 */
export const SECTION_MEDIA = {
  inicioHistoria:    HERO_IMAGES.messi,
  pollasIntensidad:  HERO_IMAGES.messiEliminacion,
  seleccionesLegado: HERO_IMAGES.baggio,
  albumLegendarios:  HERO_IMAGES.andresEscobar,
  decorativo:        HERO_IMAGES.sinIdentificar,
};

export const getSectionMedia = (key) => SECTION_MEDIA[key] || null;

export default HERO_MEDIA;
