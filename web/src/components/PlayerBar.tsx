import { useState, type CSSProperties } from "react";
import { formatDuration } from "../format";
import { usePlayer } from "../player/PlayerProvider";
import { Cover } from "./Cover";
import { Icon } from "./Icon";

/** Slider dont la partie remplie suit la valeur ; on ne "commit" qu'au relâchement pendant un glisser. */
function Slider({ value, max, onCommit, onPreview, label }: {
  value: number;
  max: number;
  onCommit: (v: number) => void;
  onPreview?: (v: number | null) => void;
  label: string;
}) {
  const [drag, setDrag] = useState<number | null>(null);
  const shown = drag ?? value;
  const fill = max > 0 ? (shown / max) * 100 : 0;

  const commit = () => {
    if (drag !== null) onCommit(drag);
    setDrag(null);
    onPreview?.(null);
  };

  return (
    <input
      type="range"
      className="slider"
      aria-label={label}
      min={0}
      max={max || 1}
      step="any"
      value={shown}
      style={{ "--fill": `${fill}%` } as CSSProperties}
      onChange={(e) => {
        const v = Number(e.target.value);
        setDrag(v);
        onPreview?.(v);
      }}
      onPointerUp={commit}
      onKeyUp={commit}
      onBlur={() => drag !== null && commit()}
    />
  );
}

export function PlayerBar() {
  const p = usePlayer();
  const [preview, setPreview] = useState<number | null>(null);
  const track = p.current;

  return (
    <footer className="player-bar">
      <div className="now-playing">
        <Cover url={track?.coverUrl} label={track?.title} size={56} />
        <div className="now-text">
          <span className="now-title">{track?.title ?? "Aucun morceau"}</span>
          <span className="now-artist">{track?.artist ?? "Choisis un titre pour l'écouter"}</span>
        </div>
      </div>

      <div className="player-center">
        <div className="controls">
          <button className={`player-btn toggle ${p.shuffle ? "active" : ""}`} onClick={p.toggleShuffle} aria-label="Lecture aléatoire" aria-pressed={p.shuffle}>
            <Icon name="shuffle" size={18} />
          </button>
          <button className="player-btn" onClick={p.previous} aria-label="Précédent">
            <Icon name="previous" size={20} />
          </button>
          <button className="play-btn" onClick={p.toggle} aria-label={p.playing ? "Pause" : "Lecture"}>
            <Icon name={p.playing ? "pause" : "play"} size={20} />
          </button>
          <button className="player-btn" onClick={p.next} aria-label="Suivant">
            <Icon name="next" size={20} />
          </button>
          <button className={`player-btn toggle ${p.repeat !== "off" ? "active" : ""}`} onClick={p.cycleRepeat}
                  aria-label={`Répéter : ${p.repeat === "off" ? "désactivé" : p.repeat === "all" ? "tout" : "un seul"}`}>
            <Icon name={p.repeat === "one" ? "repeatOne" : "repeat"} size={18} />
          </button>
        </div>
        <div className="progress">
          <span className="time-label">{formatDuration(preview ?? p.time) || "0:00"}</span>
          <Slider value={p.time} max={p.duration} onCommit={p.seek} onPreview={setPreview} label="Position" />
          <span className="time-label">{formatDuration(p.duration) || "0:00"}</span>
        </div>
      </div>

      <div className="player-right">
        <button className="player-btn" onClick={p.toggleMute} aria-label={p.volume === 0 ? "Rétablir le son" : "Couper le son"}>
          <Icon name={p.volume === 0 ? "volumeMute" : "volume"} size={20} />
        </button>
        <Slider value={p.volume} max={1} onCommit={p.setVolume} onPreview={(v) => v !== null && p.setVolume(v)} label="Volume" />
      </div>
    </footer>
  );
}
