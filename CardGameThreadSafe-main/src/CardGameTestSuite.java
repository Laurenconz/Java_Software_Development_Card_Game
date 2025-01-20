import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({PlayerTest.class, CardTest.class, CardDeckTest.class, CardGameTest.class})
public class CardGameTestSuite {
}