package re;

import fa.nfa.NFA;

import java.util.HashSet;
import java.util.Set;

import fa.State;
import fa.nfa.NFAState;

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

  private NFA term() {
    NFA nfaFactor = factor();
    while (more() && peek() != ')' && peek() != '|') {
      NFA nextFactor = factor();
      nfaFactor = sequence(nfaFactor,nextFactor) ;
    }
    return nfaFactor;
  }

  private NFA factor() {
    NFA nfaBase = base();
    while(more() && peek()=='*'){
      eat('*');
    }
    return nfaBase;
  }

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
  private char peek() {
    return input.charAt(0) ;
  }
    
  private void eat(char c) {
    if (peek() == c) {
      this.input = this.input.substring(1);
    } else {
      throw new RuntimeException("Expected: " + c + "; got: " + peek()) ;
    }
  }
  
  private char next() {
    char c = peek() ;
    eat(c) ;
    return c ;
  }

  private boolean more() {
    return input.length() > 0 ;
  }

  /* OTHER HELPERS */
  private boolean isInNFA(String name,NFA nfa) {
    for(State state : nfa.getFinalStates())
      if(state.getName().equals(name))
        return true;
    return false;
  }
}