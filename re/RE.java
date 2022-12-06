package re;

import fa.dfa.DFA;
import fa.nfa.NFA;
import fa.State;

public class RE implements REInterface{

  private NFA nfa;
  private String input ;

  public RE(String str){
      this.input = str;
  }

  @Override
  public NFA getNFA() {
      NFA nfa = regex();
      return null;
  }

  private NFA regex() {
    NFA nfaTerm = term();

    if (more() && peek() == '|') {
      eat ('|') ;
      NFA regexNFA = regex();
      NFA union = union(nfaTerm, regexNFA) ;
      return union;
    } else {
      return nfaTerm ;
    }
  }

  private NFA union(NFA first, NFA second) {
    NFA newNFA = new NFA("union");
    newNFA.addStartState();
    newNFA.addNFAStates(first.getStates());
    newNFA.addNFAStates(second.getStates());
    newNFA.addTransition(newNFA.getStartState().getName(), 'e', first.getStartState().getName());
    newNFA.addTransition(newNFA.getStartState().getName(), 'e', second.getStartState().getName());
    return newNFA;
  }

  private char peek() {
        return input.charAt(0) ;
      }
    
  private void eat(char c) {
    if (peek() == c)
      this.input = this.input.substring(1) ;
    else
      throw new 
        RuntimeException("Expected: " + c + "; got: " + peek()) ;
  }
  
  private char next() {
    char c = peek() ;
    eat(c) ;
    return c ;
  }

  private boolean more() {
    return input.length() > 0 ;
  }
    
}