package snu.swpp.moment;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
// Suite 내에 다른 테스트용 클래스 넣어서 한번에 돌릴 수 있음.
@Suite.SuiteClasses({
    LoginActivityTest.class,
    LoginRegisterActivityTest.class,
    MonthViewFragmentTest.class,
    RegisterActivityTest.class,
    SearchViewFragmentTest.class,
    StatViewFragmentTest.class,
    UserInfoViewTest.class,
    WriteViewFragmentTest.class
})
public class ActivityTestSuite {
    // Empty class
}
