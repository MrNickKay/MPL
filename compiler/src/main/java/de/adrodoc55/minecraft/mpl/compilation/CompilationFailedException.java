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
package de.adrodoc55.minecraft.mpl.compilation;

import java.io.File;
import java.util.Map.Entry;

import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;

import de.adrodoc55.minecraft.mpl.placement.NotEnoughSpaceException;

/**
 * @author Adrodoc55
 */
@Immutable
public class CompilationFailedException extends Exception {
  private static final long serialVersionUID = 1L;

  private final ImmutableListMultimap<File, CompilerException> errors;

  public CompilationFailedException(Iterable<CompilerException> errors) {
    this.errors = Multimaps.index(errors, e -> e.getSource().file);
  }

  public CompilationFailedException(String message, NotEnoughSpaceException ex) {
    super(message, ex);
    errors = ImmutableListMultimap.of();
  }

  public ImmutableListMultimap<File, CompilerException> getErrors() {
    return errors;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    String message = getLocalizedMessage();
    if (message != null) {
      sb.append(message).append("\n\n");
    }
    for (Entry<File, CompilerException> it : errors.entries()) {
      sb.append(it.getValue().toString());
      sb.append("\n\n");
    }
    return sb.toString();
  }
}
