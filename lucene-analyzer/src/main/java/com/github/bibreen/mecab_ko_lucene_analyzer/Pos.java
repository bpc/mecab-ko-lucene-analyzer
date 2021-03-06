/*******************************************************************************
 * Copyright 2013 Yongwoon Lee, Yungho Yu
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.github.bibreen.mecab_ko_lucene_analyzer;

import org.chasen.mecab.Node;
import com.github.bibreen.mecab_ko_lucene_analyzer.PosIdManager.PosId;

/**
 * 품사(형태소, 품사 ID, 위치 등) 정보 클래스
 * 
 * @author bibreen <bibreen@gmail.com>
 */
public class Pos {
  private String surface;
  private PosId posId;
  private PosId startPosId;
  private PosId endPosId;
  private int startOffset;
  private int positionIncr;
  private int positionLength;
  private String indexExpression;
  private Node node;
  
  public static class Expression {
    final static int TERM_INDEX = 0;
    final static int TAG_INDEX = 1;
    final static int POSITION_INCR_INDEX = 2;
    final static int POSITION_LENGTH_INDEX = 3;
  }
  
  public Pos(
      String surface,
      PosId posId,
      int startOffset,
      int positionIncr,
      int positionLength) {
    this.surface = surface;
    this.posId = posId;
    startPosId = posId;
    endPosId = posId;
    this.startOffset = startOffset;
    this.positionIncr = positionIncr;
    this.positionLength = positionLength;
  }
  
  /**
   * mecab의 자료 구조인 node를 사용하는 Pos 생성자.
   * 
   * @param node Node
   * @param prevEndOffset 이전 Pos의 end offset
   */
  public Pos(Node node, int prevEndOffset) {
    this(
        node.getSurface(),
        PosId.convertFrom(node.getPosid()),
        prevEndOffset + node.getRlength() - node.getLength(),
        1, 1);
    this.node = node;
    if (posId == PosId.COMPOUND ||
        posId == PosId.INFLECT ||
        posId == PosId.PREANALYSIS) {
      parseFeatureString();
    }
  }
  
  /**
   * Pos를 표현하는 문자열을 받는 Pos 생성자.
   * expression은 다음과 같이 구성된다.
   * '<surface>/<tag>/<position_incr>/<position_length>'
   * ex) 명사/NN/1/1
   */
  public Pos(String expression, int startOffset) {
    String[] datas = expression.split("/");
    this.surface = datas[Expression.TERM_INDEX];
    this.posId = PosId.convertFrom(datas[Expression.TAG_INDEX]);
    startPosId = posId;
    endPosId = posId;
    this.startOffset = startOffset;
    this.positionIncr=
        Integer.parseInt(datas[Expression.POSITION_INCR_INDEX]);
    this.positionLength =
        Integer.parseInt(datas[Expression.POSITION_LENGTH_INDEX]);
  }
  
  private void parseFeatureString() {
    final int startPosPosition = 4;
    final int endPosPosition = 5;
    final int indexExpressionPosition = 7;
    
    String feature = node.getFeature();
    String items[] = feature.split(",");
    if (items.length < indexExpressionPosition + 1) {
      throw new IllegalArgumentException(
          "Please, use higher version of mecab-ko-dic.");
    }
    if (posId == PosId.INFLECT || posId == PosId.PREANALYSIS) {
      startPosId = PosId.convertFrom(items[startPosPosition].toUpperCase());
      endPosId = PosId.convertFrom(items[endPosPosition].toUpperCase());
    } else if (posId == PosId.COMPOUND){
      startPosId = PosId.N;
      endPosId = PosId.N;
      positionLength =
          getCompoundNounPositionLength(items[indexExpressionPosition]);
    } else {
      this.startPosId = posId;
      this.endPosId = posId;
    }
    indexExpression = items[indexExpressionPosition];
  }
  
  private int getCompoundNounPositionLength(String indexExpression) {
    String firstToken = indexExpression.split("\\+")[1];
    final int postionLengthPosition = 3;
    return Integer.parseInt(firstToken.split("/")[postionLengthPosition]);
  }
  
  public Node getNode() {
    return node;
  }
  
  public PosId getPosId() {
    return posId;
  }
  
  public PosId getStartPosId() {
    return startPosId;
  }
  
  public PosId getEndPosId() {
    return endPosId;
  }
  
  public String getSurface() {
    return surface;
  }
  
  public int getSurfaceLength() {
    return surface.length();
  }
  
  public String getIndexExpression() {
    return indexExpression;
  }
  
  public int getStartOffset() {
    return startOffset;
  }
  
  public int getEndOffset() {
    return startOffset + surface.length();
  }
  
  public int getPositionIncr() {
    return positionIncr;
  }
  
  public int getPositionLength() {
    return positionLength;
  }
  
  public int getSpaceLength() {
    if (node == null) return 0;
    return node.getRlength() - node.getLength(); 
  }
  
  public int getLength() {
    return getSpaceLength() + getSurfaceLength();
  }
  
  public boolean isPosIdOf(PosId posId) {
    return (this.posId == posId);
  }
 
  public boolean hasSpace() {
    return getSpaceLength() > 0;
  }
  
  public void setStartOffset(int val) {
    startOffset = val;
  }
  
  public void setPositionIncr(int val) {
    positionIncr = val;
  }

  public void setPositionLength(int val) {
    positionLength = val;
  }

  @Override
  public String toString() {
    return new String(
        surface + "/" + posId + "/" +
        positionIncr + "/" + positionLength + "/" +
        getStartOffset() + "/" + getEndOffset());
  }
}
