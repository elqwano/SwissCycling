package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;

/**
 * présente un point de passage
 *
 * @param pointCh       la position du point de passage dans le système de coordonnées suisse
 * @param closestNodeId l'identité du nœud JaVelo le plus proche de ce point de passage
 */
public record Waypoint(PointCh pointCh, int closestNodeId) {
}
