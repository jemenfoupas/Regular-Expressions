package re;

import fa.nfa.NFA;

import java.util.HashSet;
import java.util.Set;

import fa.State;
import fa.nfa.NFAState;

/**
 * Regualar expression class that can parse a regular expression and create a NFA.
 * @author Rich Boundji
 * @author Ethan Raygor
 */
public class RE implements REInterface{

  private String input ;
  private int name;

  public RE(String str){
    this.input = str;
    this.name = 0;
  }

  @Override
  public NFA getNFA() {
    NFA nfa = regex();
    Set<Character> abSet = new HashSet<Character>();
    abSet.add('a');
    abSet.add('b');
    nfa.addAbc(abSet);
    return nfa;
  }

  /* REGEX TERM TYPES */
  /**
   * Parses a regular expression into an NFA
   * @return NFA resulting from regular expression
   */
  private NFA regex() {
    NFA nfaTerm = term();
    if (more() && peek() == '|') {
      eat ('|') ;
      NFA regexNFA = regex();
      nfaTerm = union(nfaTerm, regexNFA) ;
      return nfaTerm;
    } else {
      return nfaTerm ;
    }
  }

  /**
   * Parses a term from a regular expression
   * @return NFA resulting from term
   */
  private NFA term() {
    NFA nfaFactor = factor();
    while (more() && peek() != ')' && peek() != '|') {
      NFA nextFactor = factor();
      nfaFactor = sequence(nfaFactor,nextFactor) ;
    }
    return nfaFactor;
  }

  /**
   * Parses a factor from a term
   * @return NFA resulting from factor
   */
  private NFA factor() {
    NFA nfaBase = base();
    while(more() && peek()=='*'){
      eat('*');
    }
    return nfaBase;
  }

  /**
   * Parses a base from a factor
   * @return NFA resulting from base
   */ 
  private NFA base() {
    switch (peek()) {
      case '(':
        eat('(') ;
        NFA nfa = regex() ;
        eat(')') ;

        if(more() && peek()=='*'){
          for(State state: nfa.getFinalStates()){
            nfa.addTransition(state.getName(), 'e',nfa.getStartState().getName());
            ((NFAState) nfa.getStartState()).setFinal();
          }
        }

        return nfa ;
      default: 
        NFA nfaPrimitive = primitive(next()); 
        return nfaPrimitive;
    }
  }

  /* REGEX SUBBUILDERS */

  /**
   * Creates a union of two NFAs
   * @param firstNFA NFA
   * @param secondNFA NFA
   * @return NFA resulting from union
   */
  private NFA union(NFA firstNFA, NFA secondNFA) {
    NFA newNFA = new NFA();
    
    newNFA.addStartState(Integer.toString(this.name));
    this.name = this.name + 1;

    newNFA.addNFAStates(firstNFA.getStates());
    newNFA.addNFAStates(secondNFA.getStates());

    newNFA.addTransition(newNFA.getStartState().getName(), 'e', firstNFA.getStartState().getName());
    newNFA.addTransition(newNFA.getStartState().getName(), 'e', secondNFA.getStartState().getName());

    newNFA.addAbc(firstNFA.getABC());
    newNFA.addAbc(secondNFA.getABC());

    return newNFA;
  }

  /**
   * Puts two NFAs in sequence
   * @param firstNFA NFA
   * @param secondNFA NFA
   * @return NFA resulting from sequence
   */
  private NFA sequence(NFA firstNFA, NFA secondNFA) {
    firstNFA.addNFAStates(secondNFA.getStates());
    firstNFA.addAbc(secondNFA.getABC());

    for(State state: firstNFA.getFinalStates()){
      if(!isInNFA(state.getName(), secondNFA)){
        firstNFA.addTransition(state.getName(), 'e',secondNFA.getStartState().getName());
        ((NFAState) state).setNonFinal();
      }
      
    }
    return firstNFA;
  }

  /**
   * Creates a primitive NFA
   * @param next char
   * @return NFA resulting from primitive
   */
  private NFA primitive(char next) {
    NFA newNFA = new NFA();

    newNFA.addStartState(Integer.toString(this.name));
    this.name++;  
    ((NFAState) newNFA.getStartState()).setFinal();

    if(more() && peek()=='*'){
      newNFA.addTransition(Integer.toString(this.name - 1), next, Integer.toString(this.name - 1));
    }else{
      NFA switchNFA =  new NFA();
      switchNFA.addStartState(Integer.toString(this.name));
      this.name++; 
      switchNFA.addNFAStates(newNFA.getStates());

      switchNFA.addTransition(Integer.toString(this.name - 1), next, Integer.toString(this.name - 2));
      newNFA = switchNFA;
    }

    return newNFA;
  }

  /* DECENT PARSING INTERNALS */

  /**
   * Checks what is next in input String
   * @return char at index 0 in input
   */
  private char peek() {
    return input.charAt(0) ;
  }
    
  /**
   * Eats the next character in input if it matches the parameter
   * @param c char
   * @exception RuntimeException if the next character does not match c
   */
  private void eat(char c) {
    if (peek() == c) {
      this.input = this.input.substring(1);
    } else {
      throw new RuntimeException("Expected: " + c + "; got: " + peek()) ;
    }
  }
  
  /**
   * Eats and returns whatever character is next in input
   * @return char that was next
   */
  private char next() {
    char c = peek() ;
    eat(c) ;
    return c ;
  }

  /**
   * Returns true if more characters in input
   * @return boolean
   */
  private boolean more() {
    return input.length() > 0 ;
  }

  /* OTHER HELPERS */

  /**
   * Returns true if state with given name exists in given NFA
   * @param name String
   * @param nfa NFA
   * @return boolean
   */
  private boolean isInNFA(String name,NFA nfa) {
    for(State state : nfa.getFinalStates())
      if(state.getName().equals(name))
        return true;
    return false;
  }
}