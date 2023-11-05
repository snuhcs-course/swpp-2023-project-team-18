package snu.swpp.moment;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import snu.swpp.moment.ui.login.LoginActivity;

@RunWith(Suite.class)
//Suite 내에 다른 테스트용 클래스 넣어서 한번에 돌릴 수 있음.
@Suite.SuiteClasses({
        LoginRegisterActivityTest.class
        // , OtherTest.class // You can include additional test classes as needed
})
public class ActivityTestSuite {} // Empty class
