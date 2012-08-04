/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2012, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.subst;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.ScanException;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

  enum TokenizerState {LITERAL_STATE, START_STATE}

  final String pattern;
  final int patternLength;

  public Tokenizer(String pattern) {
    this.pattern = pattern;
    patternLength = pattern.length();
  }

  TokenizerState state = TokenizerState.LITERAL_STATE;
  int pointer = 0;

  List tokenize() throws ScanException {
    List<Token> tokenList = new ArrayList<Token>();
    StringBuilder buf = new StringBuilder();

    while (pointer < patternLength) {
      char c = pattern.charAt(pointer);
      pointer++;

      switch (state) {
        case LITERAL_STATE:
          handleLiteralState(c, tokenList, buf);
          break;
        case START_STATE:
          handleStartState(c, tokenList, buf);
          break;
        default:
      }
    }
     // EOS
    switch (state) {
      case LITERAL_STATE:
        addLiteralToken(tokenList, buf);
        break;
      case START_STATE:
        throw new ScanException("Unexpected end of pattern string");
    }
    return tokenList;
  }

  private void handleStartState(char c, List<Token> tokenList, StringBuilder stringBuilder) {
    if(c == CoreConstants.CURLY_LEFT) {
      tokenList.add(Token.START_TOKEN);
    } else {
      stringBuilder.append(CoreConstants.DOLLAR).append(c);
    }
    state = TokenizerState.LITERAL_STATE;
  }

  private void handleLiteralState(char c, List<Token> tokenList, StringBuilder stringBuilder) {
    if (c == CoreConstants.DOLLAR) {
      addLiteralToken(tokenList, stringBuilder);
      stringBuilder.setLength(0);
      state = TokenizerState.START_STATE;
    } else if (c == CoreConstants.CURLY_RIGHT) {
      addLiteralToken(tokenList, stringBuilder);
      tokenList.add(Token.STOP_TOKEN);
      stringBuilder.setLength(0);
    } else {
      stringBuilder.append(c);
    }

  }

  private void addLiteralToken(List<Token> tokenList, StringBuilder stringBuilder) {
    if (stringBuilder.length() == 0)
      return;
    tokenList.add(new Token(Token.Type.LITERAL, stringBuilder.toString()));
  }


}

