import { useState } from "react";
import { api } from "../api/client";
import { Icon } from "./Icon";

interface CoverProps {
  /** URL relative renvoyée par l'API (déjà recadrée en carré par le serveur). */
  url?: string;
  /** Texte dont l'initiale s'affiche sans pochette. */
  label?: string;
  size: number;
  round?: boolean;
}

export function Cover({ url, label, size, round }: CoverProps) {
  const [failed, setFailed] = useState(false);
  const initial = label?.trim().charAt(0).toUpperCase();

  return (
    <div className={`cover ${round ? "cover-round" : ""}`} style={{ width: size, height: size, fontSize: size * 0.38 }}>
      {url && !failed ? (
        <img src={api.url(url)} alt="" loading="lazy" onError={() => setFailed(true)} />
      ) : initial ? (
        <span className="cover-initial">{initial}</span>
      ) : (
        <Icon name="music" size={size * 0.42} className="cover-glyph" />
      )}
    </div>
  );
}
