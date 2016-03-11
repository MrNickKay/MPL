/*
 * MPL (Minecraft Programming Language): A language for easy development of commandblock
 * applications including and IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * This file is part of MPL (Minecraft Programming Language).
 *
 * MPL is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MPL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MPL. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 *
 *
 * MPL (Minecraft Programming Language): Eine Sprache für die einfache Entwicklung von Commandoblock
 * Anwendungen, beinhaltet eine IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * Diese Datei ist Teil von MPL (Minecraft Programming Language).
 *
 * MPL ist Freie Software: Sie können es unter den Bedingungen der GNU General Public License, wie
 * von der Free Software Foundation, Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
 * veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 *
 * MPL wird in der Hoffnung, dass es nützlich sein wird, aber OHNE JEDE GEWÄHRLEISTUNG,
 * bereitgestellt; sogar ohne die implizite Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN
 * BESTIMMTEN ZWECK. Siehe die GNU General Public License für weitere Details.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen mit MPL erhalten haben. Wenn
 * nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.adrodoc55.minecraft.mpl;

import static de.adrodoc55.minecraft.coordinate.Axis3D.X;
import static de.adrodoc55.minecraft.coordinate.Axis3D.Y;
import static de.adrodoc55.minecraft.coordinate.Axis3D.Z;

import java.util.Iterator;
import java.util.List;

import de.adrodoc55.minecraft.coordinate.Coordinate3D;
import de.adrodoc55.minecraft.coordinate.Orientation3D;

public class CommandBlockChain {

  private final String name;
  private final List<MplBlock> blocks;

  public CommandBlockChain(String name, List<MplBlock> commandBlocks) {
    this.name = name;
    this.blocks = commandBlocks;
  }

  /**
   * Moves this Chain according to the vector
   *
   * @param vector
   */
  public void move(Coordinate3D vector) {
    for (MplBlock block : blocks) {
      block.setCoordinate(block.getCoordinate().plus(vector));
    }
  }

  public String getName() {
    return name;
  }

  public List<MplBlock> getBlocks() {
    return blocks;
  }

  public Coordinate3D getFurthestFromStart(Orientation3D orientation) {
    Iterator<MplBlock> it = blocks.iterator();
    if (!it.hasNext()) {
      return new Coordinate3D();
    }
    MplBlock first = it.next();
    Coordinate3D pos = first.getCoordinate();
    int x = pos.getX();
    int y = pos.getY();
    int z = pos.getZ();
    while (it.hasNext()) {
      MplBlock current = it.next();
      Coordinate3D c = current.getCoordinate();
      if (orientation.get(X).isNegative()) {
        x = Math.min(x, c.getX());
      } else {
        x = Math.max(x, c.getX());
      }
      if (orientation.get(Y).isNegative()) {
        y = Math.min(y, c.getY());
      } else {
        y = Math.max(y, c.getY());
      }
      if (orientation.get(Z).isNegative()) {
        z = Math.min(z, c.getZ());
      } else {
        z = Math.max(z, c.getZ());
      }
    }
    return new Coordinate3D(x, y, z);
  }

  @Override
  public String toString() {
    return getName();
  }
}
