/*
 * Minecraft Programming Language (MPL): A language for easy development of command block
 * applications including an IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * This file is part of MPL.
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
 * Minecraft Programming Language (MPL): Eine Sprache für die einfache Entwicklung von Commandoblock
 * Anwendungen, inklusive einer IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * Diese Datei ist Teil von MPL.
 *
 * MPL ist freie Software: Sie können diese unter den Bedingungen der GNU General Public License,
 * wie von der Free Software Foundation, Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
 * veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 *
 * MPL wird in der Hoffnung, dass es nützlich sein wird, aber OHNE JEDE GEWÄHRLEISTUNG,
 * bereitgestellt; sogar ohne die implizite Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN
 * BESTIMMTEN ZWECK. Siehe die GNU General Public License für weitere Details.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen mit MPL erhalten haben. Wenn
 * nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.adrodoc55.minecraft.mpl.commands.chainlinks;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

import de.adrodoc55.commons.CopyScope;
import de.adrodoc55.minecraft.coordinate.Coordinate3D;
import de.adrodoc55.minecraft.coordinate.Direction3D;
import de.adrodoc55.minecraft.mpl.blocks.CommandBlock;
import de.adrodoc55.minecraft.mpl.blocks.MplBlock;
import de.adrodoc55.minecraft.mpl.commands.Mode;
import de.adrodoc55.minecraft.mpl.commands.Modifiable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.karneim.pojobuilder.GenerateMplPojoBuilder;

/**
 * @author Adrodoc55
 */
@EqualsAndHashCode
@ToString
@Getter
@Setter
public class Command implements ChainLink, Modifiable {
  protected @Nonnull String command;
  protected @Nonnull Mode mode;
  protected boolean conditional;
  protected boolean needsRedstone;

  public Command() {
    this("");
  }

  public Command(String command) {
    this(command, null, false);
  }

  public Command(String command, Mode mode) {
    this(command, mode, false);
  }

  public Command(String command, boolean conditional) {
    this(command, null, conditional);
  }

  public Command(String command, Mode mode, boolean conditional) {
    this(command, mode, conditional, Mode.nonNull(mode).getNeedsRedstoneByDefault());
  }

  @GenerateMplPojoBuilder
  public Command(String command, Mode mode, boolean conditional, boolean needsRedstone) {
    setCommand(command);
    setModifier(mode, conditional, needsRedstone);
  }

  public Command(String command, Modifiable modifier) {
    setCommand(command);
    setModifier(modifier);
  }

  @Deprecated
  protected Command(Command original) {
    command = original.command;
    mode = original.mode;
    conditional = original.conditional;
    needsRedstone = original.needsRedstone;
  }

  @Deprecated
  @Override
  public Command createFlatCopy(CopyScope scope) {
    return new Command(this);
  }

  public void setModifier(Modifiable modifier) {
    setModifier(modifier.getMode(), modifier.isConditional(), modifier.getNeedsRedstone());
  }

  public void setModifier(Mode mode, boolean conditional, boolean needsRedstone) {
    this.mode = Mode.nonNull(mode);
    this.conditional = conditional;
    this.needsRedstone = needsRedstone;
  }

  public void setCommand(String command) {
    checkNotNull(command, "command == null!");
    if (command.startsWith("/")) {
      this.command = command.substring(1);
    } else {
      this.command = command;
    }
  }

  @Override
  public @Nonnull Boolean isConditional() {
    return conditional;
  }

  @Override
  public @Nonnull Boolean getNeedsRedstone() {
    return needsRedstone;
  }

  @Override
  public MplBlock toBlock(Coordinate3D coordinate) {
    // FIXME: Direction korrigieren
    return new CommandBlock(this, Direction3D.UP, coordinate);
  }

  public boolean isInternal() {
    return false;
  }
}
