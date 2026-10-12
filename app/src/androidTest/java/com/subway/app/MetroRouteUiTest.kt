package com.subway.app

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.rule.GrantPermissionRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MetroRouteUiTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun resetAppState() {
        val context = androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation()
            .targetContext
        context.getSharedPreferences("auth_prefs", 0).edit().clear().commit()
        scenario = ActivityScenario.launch(MainActivity::class.java)
        composeRule.waitForIdle()
    }

    @After
    fun closeActivity() {
        scenario.close()
    }

    @Test
    fun loginPageShowsRequiredControls() {
        composeRule.onNodeWithText("武汉地铁路路通").assertIsDisplayed()
        composeRule.onNodeWithText("账号").assertIsDisplayed()
        composeRule.onNodeWithText("密码").assertIsDisplayed()
        composeRule.onNodeWithText("登录").assertIsDisplayed()
        composeRule.onNodeWithText("注册账号").assertIsDisplayed()
        composeRule.onNodeWithText("忘记密码").assertIsDisplayed()
    }

    @Test
    fun emptyLoginShowsAccountValidationMessage() {
        composeRule.onNodeWithText("登录").performClick()
        composeRule.onNodeWithText("请输入账号").assertIsDisplayed()
    }

    @Test
    fun emptyPasswordShowsPasswordValidationMessage() {
        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("admin")
        composeRule.onNodeWithText("登录").performClick()
        composeRule.onNodeWithText("请输入密码").assertIsDisplayed()
    }

    @Test
    fun wrongPasswordShowsCredentialsError() {
        val fields = composeRule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("admin")
        fields[1].performTextInput("000000")
        composeRule.onNodeWithText("登录").performClick()
        composeRule.onNodeWithText("账号或密码错误").assertIsDisplayed()
    }

    @Test
    fun registerAccountThenLoginSucceeds() {
        composeRule.onAllNodesWithText("注册账号").onFirst().performClick()
        val fields = composeRule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("ui_test_user")
        fields[1].performTextInput("metro123")
        composeRule.onAllNodesWithText("注册账号").onLast().performClick()
        composeRule.onNodeWithText("注册成功，请登录").assertIsDisplayed()

        composeRule.onNodeWithText("登录").performClick()
        composeRule.onNodeWithText("搜索站点、线路").assertIsDisplayed()
    }

    @Test
    fun duplicateAccountRegistrationIsRejected() {
        composeRule.onAllNodesWithText("注册账号").onFirst().performClick()
        val fields = composeRule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("admin")
        fields[1].performTextInput("another1")
        composeRule.onAllNodesWithText("注册账号").onLast().performClick()
        composeRule.onNodeWithText("账号已存在").assertIsDisplayed()
    }

    @Test
    fun invalidRegistrationFormatIsRejected() {
        composeRule.onAllNodesWithText("注册账号").onFirst().performClick()
        val fields = composeRule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("ab")
        fields[1].performTextInput("123456")
        composeRule.onAllNodesWithText("注册账号").onLast().performClick()
        composeRule.onNodeWithText("账号需为3-20位字母/数字/下划线，密码至少6位").assertIsDisplayed()
    }

    @Test
    fun correctLoginOpensHome() {
        loginAsAdmin()
        composeRule.onNodeWithText("13条线路  335个站点").assertIsDisplayed()
        composeRule.onNodeWithText("搜索站点、线路").assertIsDisplayed()
        composeRule.onNodeWithText("换乘规划").assertIsDisplayed()
    }

    @Test
    fun lineListLoadsAndOpensLineDetail() {
        loginAsAdmin()
        composeRule.onNodeWithText("全部线路").performClick()
        composeRule.onNodeWithText("1号线").assertIsDisplayed()
        composeRule.onNodeWithText("2号线").assertIsDisplayed()
        composeRule.onNodeWithText("1号线").performClick()
        composeRule.onNodeWithText("途经站点（32站）").assertIsDisplayed()
        composeRule.onAllNodesWithText("径河").onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("汉口北").onFirst().assertIsDisplayed()
    }

    @Test
    fun searchPageShowsHotSearchAndSupportsQuery() {
        loginAsAdmin()
        composeRule.onNodeWithText("搜索站点").performClick()
        composeRule.onNodeWithText("热门搜索").assertIsDisplayed()
        val searchField = composeRule.onAllNodes(hasSetTextAction())[0]
        searchField.performTextInput("江汉路")
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText("江汉路").onLast().assertIsDisplayed()
    }

    @Test
    fun searchWithoutMatchesShowsEmptyState() {
        loginAsAdmin()
        composeRule.onNodeWithText("搜索站点").performClick()
        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("不存在的站点xyz")
        composeRule.onNodeWithText("未找到结果").assertIsDisplayed()
    }

    @Test
    fun lineStationsAreDisplayedFromFirstToLast() {
        loginAsAdmin()
        composeRule.onNodeWithText("全部线路").performClick()
        composeRule.onNodeWithText("2号线").performClick()
        composeRule.onNodeWithText("途经站点（38站）").assertIsDisplayed()
        composeRule.onAllNodesWithText("天河机场").onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("佛祖岭").onFirst().assertIsDisplayed()
    }

    @Test
    fun stationSearchResultOpensStationDetails() {
        loginAsAdmin()
        composeRule.onNodeWithText("搜索站点").performClick()
        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("江汉路")
        composeRule.onAllNodesWithText("江汉路").onLast().performClick()
        composeRule.onAllNodesWithText("所属线路").onFirst().assertIsDisplayed()
        composeRule.onNodeWithText("运营时间").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("英文名称").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun routePageShowsStartAndEndSelectors() {
        loginAsAdmin()
        composeRule.onNodeWithText("换乘规划").performClick()
        composeRule.onNodeWithText("换乘规划").assertIsDisplayed()
        composeRule.onNodeWithText("起点").assertIsDisplayed()
        composeRule.onNodeWithText("终点").assertIsDisplayed()
        composeRule.onNodeWithText("查询路线").assertIsDisplayed()
    }

    @Test
    fun routeQueryIsDisabledUntilBothStationsAreSelected() {
        loginAsAdmin()
        composeRule.onNodeWithText("换乘规划").performClick()
        composeRule.onNodeWithText("查询路线").assertIsNotEnabled()
    }

    @Test
    fun routeSearchShowsRecommendedRouteAndFare() {
        loginAsAdmin()
        composeRule.onNodeWithText("换乘规划").performClick()
        selectStation("街道口")
        selectStation("江汉路")
        composeRule.onNodeWithText("查询路线").performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("推荐路线").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("推荐路线").assertIsDisplayed()
        composeRule.onNodeWithText("票价").assertIsDisplayed()
        composeRule.onAllNodesWithText("街道口").onLast().assertIsDisplayed()
        composeRule.onAllNodesWithText("江汉路").onLast().assertIsDisplayed()
    }

    @Test
    fun sameStationRouteShowsValidationError() {
        loginAsAdmin()
        composeRule.onNodeWithText("换乘规划").performClick()
        selectStation("街道口")
        selectStation("街道口")
        composeRule.onNodeWithText("查询路线").performClick()
        composeRule.onNodeWithText("起终点不能相同").assertIsDisplayed()
    }

    @Test
    fun profilePageOpensSettings() {
        loginAsAdmin()
        composeRule.onNodeWithText("我的").performClick()
        composeRule.onNodeWithText("设置").performClick()
        composeRule.onNodeWithText("账号管理").assertIsDisplayed()
        composeRule.onNodeWithText("admin").assertIsDisplayed()
        composeRule.onNodeWithText("通用设置").assertIsDisplayed()
        composeRule.onNodeWithText("深色").assertIsDisplayed()
        composeRule.onNodeWithText("字体大小").assertIsDisplayed()
        composeRule.onNodeWithText("缓存管理").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("关于我们").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("退出登录").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun logoutCanBeCancelledWithoutClearingSession() {
        loginAsAdmin()
        composeRule.onNodeWithText("我的").performClick()
        composeRule.onNodeWithText("设置").performClick()
        composeRule.onNodeWithText("退出登录").performScrollTo().performClick()
        composeRule.onNodeWithText("确认退出登录").assertIsDisplayed()
        composeRule.onNodeWithText("取消").performClick()
        composeRule.onNodeWithText("设置").assertIsDisplayed()
        composeRule.onNodeWithText("admin").assertIsDisplayed()
    }

    @Test
    fun samePasswordChangeIsRejected() {
        loginAsAdmin()
        composeRule.onNodeWithText("我的").performClick()
        composeRule.onNodeWithText("设置").performClick()
        composeRule.onNodeWithText("账号管理").performClick()
        val fields = composeRule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("123456")
        fields[1].performTextInput("123456")
        fields[2].performTextInput("123456")
        composeRule.onNodeWithText("确认修改").performClick()
        composeRule.onNodeWithText("新密码不能与当前密码相同").assertIsDisplayed()
    }

    @Test
    fun successfulPasswordChangeReturnsToLogin() {
        loginAsAdmin()
        composeRule.onNodeWithText("我的").performClick()
        composeRule.onNodeWithText("设置").performClick()
        composeRule.onNodeWithText("账号管理").performClick()
        val fields = composeRule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("123456")
        fields[1].performTextInput("654321")
        fields[2].performTextInput("654321")
        composeRule.onNodeWithText("确认修改").performClick()
        composeRule.onNodeWithText("登录").assertIsDisplayed()
        val loginFields = composeRule.onAllNodes(hasSetTextAction())
        loginFields[0].performTextInput("admin")
        loginFields[1].performTextInput("123456")
        composeRule.onNodeWithText("登录").performClick()
        composeRule.onNodeWithText("账号或密码错误").assertIsDisplayed()
        composeRule.onAllNodes(hasSetTextAction())[1].performTextClearance()
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput("654321")
        composeRule.onNodeWithText("登录").performClick()
        composeRule.onNodeWithText("搜索站点、线路").assertIsDisplayed()
    }

    @Test
    fun settingsLogoutReturnsToLoginPage() {
        loginAsAdmin()
        composeRule.onNodeWithText("我的").performClick()
        composeRule.onNodeWithText("设置").performClick()
        composeRule.onNodeWithText("退出登录").performClick()
        composeRule.onNodeWithText("确认退出登录").assertIsDisplayed()
        composeRule.onNodeWithText("退出", useUnmergedTree = true).performClick()
        composeRule.onNodeWithText("登录").assertIsDisplayed()
    }

    private fun loginAsAdmin() {
        val fields = composeRule.onAllNodes(hasSetTextAction())
        composeRule.waitUntil(5_000) {
            fields.fetchSemanticsNodes().size >= 2
        }
        fields[0].performTextInput("admin")
        fields[1].performTextInput("123456")
        composeRule.onNodeWithText("登录").performClick()
        composeRule.onNodeWithText("搜索站点、线路").assertIsDisplayed()
    }

    private fun selectStation(name: String) {
        composeRule.onAllNodesWithText("请选择站点")[0].performClick()
        composeRule.onNodeWithText("搜索站点...").performClick()
        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput(name)
        composeRule.onAllNodesWithText(name).onLast().assertIsDisplayed()
        composeRule.onAllNodesWithText(name).onLast().performClick()
    }
}
