package com.subway.app.data

import com.subway.app.data.local.entity.ConnectionEntity
import com.subway.app.data.local.entity.LineEntity
import com.subway.app.data.local.entity.StationEntity
import kotlin.math.*

object MetroDataInitializer {

    fun getInitialLines(): List<LineEntity> = listOf(
        LineEntity(1, "1号线", "#00A0E9"),
        LineEntity(2, "2号线", "#F39800"),
        LineEntity(3, "3号线", "#D10059"),
        LineEntity(4, "4号线", "#009688"),
        LineEntity(5, "5号线", "#C8A2C8"),
        LineEntity(6, "6号线", "#FF6B00"),
        LineEntity(7, "7号线", "#FFD400"),
        LineEntity(8, "8号线", "#00BFFF")
    )

    fun getInitialStations(): List<StationEntity> {
        val rawStations = listOf(
            StationEntity(101, "\u5f84\u6cb3", "1", 30.648, 114.101),
            StationEntity(102, "\u4e09\u5e97", "1", 30.642, 114.108),
            StationEntity(103, "\u7801\u5934\u6f6d\u516c\u56ed", "1,6", 30.636, 114.112),
            StationEntity(104, "\u4e1c\u5434\u5927\u9053", "1", 30.628, 114.142),
            StationEntity(105, "\u4e94\u73af\u5927\u9053", "1", 30.618, 114.155),
            StationEntity(106, "\u989d\u5934\u6e7e", "1", 30.610, 114.168),
            StationEntity(107, "\u7af9\u53f6\u6d77", "1", 30.605, 114.180),
            StationEntity(108, "\u8235\u843d\u53e3", "1", 30.598, 114.195),
            StationEntity(109, "\u53e4\u7530\u4e00\u8def", "1", 30.590, 114.212),
            StationEntity(110, "\u53e4\u7530\u4e8c\u8def", "1", 30.586, 114.225),
            StationEntity(111, "\u53e4\u7530\u4e09\u8def", "1", 30.583, 114.238),
            StationEntity(112, "\u53e4\u7530\u56db\u8def", "1", 30.580, 114.250),
            StationEntity(113, "\u6c49\u897f\u4e00\u8def", "1", 30.578, 114.262),
            StationEntity(114, "\u5b97\u5173", "1,3", 30.585, 114.218),
            StationEntity(115, "\u592a\u5e73\u6d0b", "1", 30.587, 114.275),
            StationEntity(116, "\u786c\u53e3\u8def", "1", 30.588, 114.280),
            StationEntity(117, "\u5d07\u4ec1\u8def", "1", 30.589, 114.282),
            StationEntity(118, "\u5229\u6d4e\u5317\u8def", "1", 30.590, 114.283),
            StationEntity(119, "\u53cb\u8c0a\u8def", "1", 30.591, 114.284),
            StationEntity(120, "\u5faa\u793c\u95e8", "1,2", 30.589, 114.284),
            StationEntity(121, "\u5927\u667a\u8def", "1,6", 30.595, 114.295),
            StationEntity(122, "\u4e09\u9633\u8def", "1,7", 30.600, 114.302),
            StationEntity(123, "\u9ec4\u6d66\u8def", "1,8", 30.605, 114.310),
            StationEntity(124, "\u5934\u9053\u8857", "1", 30.612, 114.322),
            StationEntity(125, "\u4e8c\u4e03\u8def", "1", 30.618, 114.330),
            StationEntity(126, "\u5f90\u5dde\u65b0\u6751", "1", 30.625, 114.340),
            StationEntity(127, "\u4e39\u6c34\u6c60", "1", 30.632, 114.348),
            StationEntity(128, "\u65b0\u8363", "1", 30.640, 114.358),
            StationEntity(129, "\u5824\u89d2", "1", 30.648, 114.365),
            StationEntity(130, "\u6ed5\u5b50\u5c97", "1", 30.656, 114.372),
            StationEntity(131, "\u6ec4\u53e3\u65b0\u57ce", "1", 30.665, 114.380),
            StationEntity(132, "\u6c49\u53e3\u5317", "1", 30.675, 114.390),
            StationEntity(201, "\u5929\u6cb3\u673a\u573a", "2", 30.774, 114.218),
            StationEntity(202, "\u822a\u7a7a\u603b\u90e8", "2", 30.755, 114.222),
            StationEntity(203, "\u5b8b\u5bb6\u5c97", "2", 30.735, 114.226),
            StationEntity(204, "\u5de8\u9f99\u5927\u9053", "2", 30.712, 114.235),
            StationEntity(205, "\u76d8\u9f99\u57ce", "2", 30.695, 114.242),
            StationEntity(206, "\u5b8f\u56fe\u5927\u9053", "2,3,8", 30.668, 114.277),
            StationEntity(207, "\u5e38\u9752\u57ce", "2", 30.655, 114.245),
            StationEntity(208, "\u91d1\u94f6\u6f6d", "2", 30.648, 114.240),
            StationEntity(209, "\u5e38\u9752\u82b1\u56ed", "2,6", 30.642, 114.248),
            StationEntity(210, "\u957f\u6e2f\u8def", "2", 30.628, 114.255),
            StationEntity(211, "\u6c49\u53e3\u706b\u8f66\u7ad9", "2", 30.618, 114.260),
            StationEntity(212, "\u8303\u6e56", "2,3", 30.610, 114.265),
            StationEntity(213, "\u738b\u5bb6\u58a9\u4e1c", "2,7", 30.600, 114.268),
            StationEntity(214, "\u9752\u5e74\u8def", "2", 30.592, 114.272),
            StationEntity(215, "\u4e2d\u5c71\u516c\u56ed", "2", 30.588, 114.275),
            StationEntity(216, "\u6c5f\u6c49\u8def", "2,6", 30.582, 114.292),
            StationEntity(217, "\u79ef\u7389\u6865", "2,5", 30.570, 114.305),
            StationEntity(218, "\u8783\u87f9\u5c6c", "2,7", 30.558, 114.315),
            StationEntity(219, "\u5c0f\u9f9f\u5c71", "2", 30.550, 114.322),
            StationEntity(220, "\u6d2a\u5c71\u5e7f\u573a", "2,4", 30.547, 114.337),
            StationEntity(221, "\u4e2d\u5357\u8def", "2,4", 30.542, 114.340),
            StationEntity(222, "\u5b9d\u901a\u5bfa", "2", 30.530, 114.345),
            StationEntity(223, "\u8857\u9053\u53e3", "2,8", 30.520, 114.355),
            StationEntity(224, "\u5e7f\u57e0\u5c6f", "2", 30.515, 114.365),
            StationEntity(225, "\u864e\u6cc9", "2", 30.505, 114.378),
            StationEntity(226, "\u6768\u5bb6\u6e7e", "2", 30.500, 114.388),
            StationEntity(227, "\u5149\u8c37\u5e7f\u573a", "2", 30.508, 114.397),
            StationEntity(301, "\u5e02\u6c11\u4e4b\u5bb6", "3", 30.661, 114.295),
            StationEntity(302, "\u540e\u6e56\u5927\u9053", "3", 30.652, 114.305),
            StationEntity(303, "\u5174\u4e1a\u8def", "3", 30.642, 114.312),
            StationEntity(304, "\u4e8c\u4e03\u5c0f\u8def", "3", 30.628, 114.318),
            StationEntity(305, "\u7f57\u5bb6\u5e84", "3", 30.618, 114.322),
            StationEntity(306, "\u8d75\u5bb6\u6761", "3,8", 30.608, 114.315),
            StationEntity(307, "\u60e0\u6d4e\u4e8c\u8def", "3", 30.602, 114.302),
            StationEntity(308, "\u9999\u6e2f\u8def", "3,6,7", 30.598, 114.295),
            StationEntity(309, "\u83f1\u89d2\u6e56", "3", 30.605, 114.275),
            StationEntity(310, "\u4e91\u98de\u8def", "3", 30.608, 114.250),
            StationEntity(311, "\u6b66\u6c49\u5546\u52a1\u533a", "3,7", 30.602, 114.242),
            StationEntity(312, "\u53cc\u58a9", "3", 30.592, 114.228),
            StationEntity(313, "\u738b\u5bb6\u6e7e", "3,4", 30.562, 114.205),
            StationEntity(314, "\u9f99\u9633\u6751", "3", 30.552, 114.198),
            StationEntity(315, "\u9676\u5bb6\u5cad", "3", 30.542, 114.190),
            StationEntity(316, "\u56db\u65b0\u5927\u9053", "3", 30.530, 114.180),
            StationEntity(317, "\u6c49\u9633\u5ba2\u8fd0\u7ad9", "3", 30.518, 114.172),
            StationEntity(318, "\u4e09\u89d2\u6e56", "3", 30.505, 114.165),
            StationEntity(319, "\u4f53\u80b2\u4e2d\u5fc3", "3", 30.495, 114.160),
            StationEntity(320, "\u4e1c\u98ce\u516c\u53f8", "3,6", 30.485, 114.155),
            StationEntity(321, "\u6c8c\u9633\u5927\u9053", "3", 30.475, 114.148),
            StationEntity(401, "\u6b66\u6c49\u706b\u8f66\u7ad9", "4", 30.608, 114.430),
            StationEntity(402, "\u6768\u6625\u6e56", "4", 30.600, 114.420),
            StationEntity(403, "\u5de5\u4e1a\u56db\u8def", "4", 30.590, 114.405),
            StationEntity(404, "\u4ec1\u548c\u8def", "4", 30.580, 114.388),
            StationEntity(405, "\u56ed\u6797\u8def", "4", 30.572, 114.375),
            StationEntity(406, "\u7f57\u5bb6\u6e2f", "4", 30.565, 114.365),
            StationEntity(407, "\u94c1\u673a\u8def", "4", 30.575, 114.355),
            StationEntity(408, "\u5cb3\u5bb6\u5634", "4,8", 30.578, 114.363),
            StationEntity(409, "\u4e1c\u4ead", "4", 30.568, 114.352),
            StationEntity(410, "\u9752\u9c7c\u5634", "4", 30.560, 114.345),
            StationEntity(411, "\u695a\u6cb3\u6c49\u8857", "4", 30.552, 114.342),
            StationEntity(412, "\u6885\u82d1\u5c0f\u533a", "4", 30.535, 114.328),
            StationEntity(413, "\u6b66\u660c\u706b\u8f66\u7ad9", "4,7", 30.531, 114.316),
            StationEntity(414, "\u9996\u4e49\u8def", "4", 30.528, 114.308),
            StationEntity(415, "\u590d\u5174\u8def", "4,5", 30.525, 114.300),
            StationEntity(416, "\u62e6\u6c5f\u8def", "4", 30.538, 114.275),
            StationEntity(417, "\u949f\u5bb6\u6751", "4,6", 30.548, 114.268),
            StationEntity(418, "\u6c49\u9633\u706b\u8f66\u7ad9", "4", 30.552, 114.255),
            StationEntity(419, "\u4e94\u91cc\u58a9", "4", 30.555, 114.242),
            StationEntity(420, "\u4e03\u91cc\u5e99", "4", 30.558, 114.228),
            StationEntity(421, "\u5341\u91cc\u94fa", "4", 30.560, 114.215),
            StationEntity(422, "\u7389\u9f99\u8def", "4", 30.565, 114.195),
            StationEntity(423, "\u6c38\u5b89\u5802", "4", 30.568, 114.185),
            StationEntity(424, "\u5b5f\u5bb6\u94fa", "4", 30.572, 114.175),
            StationEntity(425, "\u9ec4\u91d1\u53e3", "4", 30.578, 114.165),
            StationEntity(426, "\u65b0\u5929", "4", 30.585, 114.148),
            StationEntity(427, "\u96c6\u8d24", "4", 30.590, 114.130),
            StationEntity(428, "\u77e5\u97f3", "4", 30.595, 114.112),
            StationEntity(429, "\u65b0\u519c", "4", 30.600, 114.095),
            StationEntity(430, "\u51e4\u51f0\u8def", "4", 30.605, 114.078),
            StationEntity(431, "\u67cf\u6797", "4", 30.610, 114.060),
            StationEntity(501, "\u6b66\u6c49\u7ad9\u4e1c\u5e7f\u573a", "5", 30.605, 114.435),
            StationEntity(502, "\u5382\u524d", "5", 30.595, 114.420),
            StationEntity(503, "\u6b66\u94a2", "5", 30.585, 114.405),
            StationEntity(504, "\u5de5\u4eba\u6751", "5", 30.575, 114.390),
            StationEntity(505, "\u9752\u5b9c\u5c45", "5", 30.565, 114.375),
            StationEntity(506, "\u7ea2\u94a2\u57ce", "5", 30.555, 114.360),
            StationEntity(507, "\u548c\u5e73\u516c\u56ed", "5", 30.550, 114.350),
            StationEntity(508, "\u5efa\u8bbe\u4e8c\u8def", "5", 30.555, 114.345),
            StationEntity(509, "\u79d1\u666e\u516c\u56ed", "5", 30.565, 114.355),
            StationEntity(510, "\u4f59\u5bb6\u5934", "5", 30.575, 114.342),
            StationEntity(511, "\u6768\u56ed\u94c1\u56db\u9662", "5", 30.582, 114.335),
            StationEntity(512, "\u5f90\u5bb6\u68da", "5,7,8", 30.594, 114.337),
            StationEntity(513, "\u4e09\u89d2\u8def", "5", 30.578, 114.325),
            StationEntity(514, "\u4e09\u5c42\u697c", "5", 30.570, 114.315),
            StationEntity(515, "\u6619\u534e\u6797\u6b66\u80dc\u95e8", "5", 30.560, 114.298),
            StationEntity(516, "\u53f8\u95e8\u53e3\u9ec4\u9e64\u697c", "5", 30.549, 114.300),
            StationEntity(517, "\u5f6d\u5218\u6768", "5", 30.540, 114.298),
            StationEntity(518, "\u516b\u94fa\u8857", "5", 30.515, 114.302),
            StationEntity(519, "\u70fd\u706b\u6751", "5", 30.498, 114.308),
            StationEntity(520, "\u5f20\u5bb6\u6e7e", "5", 30.490, 114.312),
            StationEntity(521, "\u5149\u971e", "5", 30.482, 114.318),
            StationEntity(522, "\u767d\u6c99\u516d\u8def", "5", 30.475, 114.325),
            StationEntity(523, "\u4e2d\u533b\u836f\u5927\u5b66", "5", 30.485, 114.328),
            StationEntity(601, "\u65b0\u57ce\u5341\u4e00\u8def", "6", 30.660, 114.095),
            StationEntity(602, "\u4e94\u73af\u4f53\u80b2\u4e2d\u5fc3", "6", 30.645, 114.125),
            StationEntity(603, "\u4e8c\u96c5\u8def", "6", 30.650, 114.135),
            StationEntity(604, "\u6d77\u53e3\u4e09\u8def", "6", 30.652, 114.148),
            StationEntity(605, "\u91d1\u94f6\u6e56\u516c\u56ed", "6", 30.652, 114.172),
            StationEntity(606, "\u91d1\u94f6\u6e56", "6", 30.650, 114.195),
            StationEntity(607, "\u56ed\u535a\u56ed\u5317", "6,7", 30.633, 114.215),
            StationEntity(608, "\u8f7b\u5de5\u5927\u5b66", "6", 30.635, 114.228),
            StationEntity(609, "\u6768\u6c58\u6e56", "6", 30.632, 114.265),
            StationEntity(610, "\u77f3\u6865", "6", 30.625, 114.275),
            StationEntity(611, "\u5510\u5bb6\u58a9", "6", 30.615, 114.280),
            StationEntity(612, "\u4e09\u773c\u6865", "6", 30.608, 114.288),
            StationEntity(613, "\u82d7\u6817\u8def", "6", 30.595, 114.285),
            StationEntity(614, "\u516d\u6e21\u6865", "6", 30.578, 114.285),
            StationEntity(615, "\u6c49\u6b63\u8857", "6", 30.572, 114.280),
            StationEntity(616, "\u6b66\u80dc\u8def", "6", 30.575, 114.275),
            StationEntity(617, "\u7434\u53f0", "6", 30.565, 114.245),
            StationEntity(618, "\u9a6c\u9e66\u8def", "6", 30.540, 114.258),
            StationEntity(619, "\u5efa\u6e2f", "6", 30.528, 114.248),
            StationEntity(620, "\u524d\u8fdb\u6751", "6", 30.518, 114.238),
            StationEntity(621, "\u56fd\u535a\u4e2d\u5fc3\u5317", "6", 30.510, 114.225),
            StationEntity(622, "\u56fd\u535a\u4e2d\u5fc3\u5357", "6", 30.505, 114.218),
            StationEntity(623, "\u8001\u5173\u6751", "6", 30.495, 114.205),
            StationEntity(624, "\u6c5f\u57ce\u5927\u9053", "6", 30.488, 114.195),
            StationEntity(625, "\u8f66\u57ce\u4e1c\u8def", "6", 30.480, 114.180),
            StationEntity(701, "\u6a2a\u5e97", "7", 30.740, 114.295),
            StationEntity(702, "\u88d5\u798f\u8def", "7", 30.725, 114.290),
            StationEntity(703, "\u5929\u9633\u5927\u9053", "7", 30.710, 114.285),
            StationEntity(704, "\u817e\u9f99\u5927\u9053", "7", 30.690, 114.280),
            StationEntity(705, "\u5de8\u9f99\u5927\u90537", "7", 30.670, 114.275),
            StationEntity(706, "\u6c64\u4e91\u6d77", "7", 30.648, 114.270),
            StationEntity(707, "\u9a6c\u6c60", "7", 30.635, 114.262),
            StationEntity(708, "\u56ed\u535a\u56ed", "7", 30.628, 114.235),
            StationEntity(709, "\u5e38\u7801\u5934", "7", 30.615, 114.225),
            StationEntity(710, "\u53d6\u6c34\u697c", "7", 30.598, 114.278),
            StationEntity(711, "\u6e56\u5317\u5927\u5b66", "7", 30.582, 114.342),
            StationEntity(712, "\u65b0\u6cb3\u8857", "7", 30.572, 114.338),
            StationEntity(713, "\u5c0f\u4e1c\u95e8", "7", 30.548, 114.312),
            StationEntity(714, "\u745e\u5b89\u8857", "7", 30.512, 114.312),
            StationEntity(715, "\u5efa\u5b89\u8857", "7", 30.502, 114.315),
            StationEntity(716, "\u6e56\u5de5\u5927", "7", 30.492, 114.312),
            StationEntity(717, "\u677f\u6865", "7", 30.482, 114.308),
            StationEntity(718, "\u91ce\u82b7\u6e56", "7,8", 30.468, 114.305),
            StationEntity(719, "\u65b0\u8def\u6751", "7", 30.452, 114.300),
            StationEntity(720, "\u5927\u82b1\u5cad", "7", 30.440, 114.295),
            StationEntity(721, "\u6c5f\u590f\u5ba2\u5385", "7", 30.428, 114.290),
            StationEntity(722, "\u8c2d\u946b\u57f9\u516c\u56ed", "7", 30.415, 114.285),
            StationEntity(723, "\u5317\u534e\u8857", "7", 30.405, 114.280),
            StationEntity(724, "\u7eb8\u574a\u5927\u8857", "7", 30.395, 114.275),
            StationEntity(725, "\u9752\u9f99\u5c71\u5730\u94c1\u5c0f\u9547", "7", 30.385, 114.270),
            StationEntity(801, "\u91d1\u6f6d\u8def", "8", 30.685, 114.320),
            StationEntity(802, "\u5854\u5b50\u6e56", "8", 30.658, 114.285),
            StationEntity(803, "\u4e2d\u4e00\u8def", "8", 30.645, 114.290),
            StationEntity(804, "\u7af9\u53f6\u5c71", "8", 30.632, 114.295),
            StationEntity(805, "\u5f90\u4e1c", "8", 30.585, 114.345),
            StationEntity(806, "\u6c6a\u5bb6\u58a9", "8", 30.578, 114.350),
            StationEntity(807, "\u68a8\u56ed", "8", 30.575, 114.380),
            StationEntity(808, "\u7701\u535a\u6e56\u5317\u65e5\u62a5", "8", 30.568, 114.372),
            StationEntity(809, "\u4e2d\u5357\u533b\u9662", "8", 30.560, 114.365),
            StationEntity(810, "\u6c34\u679c\u6e56", "8", 30.552, 114.355),
            StationEntity(811, "\u6d2a\u5c71\u8def", "8", 30.548, 114.348),
            StationEntity(812, "\u5c0f\u6d2a\u5c71", "8", 30.540, 114.342),
            StationEntity(813, "\u9a6c\u623f\u5c71", "8", 30.510, 114.345),
            StationEntity(814, "\u6587\u6cbb\u8857", "8", 30.495, 114.338),
            StationEntity(815, "\u6587\u660c\u8def", "8", 30.482, 114.330),
            StationEntity(816, "\u7701\u519c\u79d1\u9662", "8", 30.472, 114.322),
            StationEntity(817, "\u9a6c\u6e56", "8", 30.465, 114.315),
            StationEntity(818, "\u9ec4\u5bb6\u6e56\u5730\u94c1\u5c0f\u9547", "8", 30.452, 114.295),
            StationEntity(819, "\u519b\u8fd0\u6751", "8", 30.438, 114.285)
        )
        // Compute first/last train times for all stations
        val times = computeTrainTimes(rawStations)
        return rawStations.map { st ->
            val (ft, lt) = times[st.stationId] ?: Pair("06:00", "22:30")
            st.copy(firstTrain = ft, lastTrain = lt)
        }
    }

    val lineSequences = mapOf(
        1 to listOf(101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132),
        2 to listOf(201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,120,216,217,218,219,220,221,222,223,224,225,226,227),
        3 to listOf(206,301,302,303,304,305,306,307,308,309,212,310,311,312,114,313,314,315,316,317,318,319,320,321),
        4 to listOf(401,402,403,404,405,406,407,408,409,410,411,220,221,412,413,414,415,416,417,418,419,420,421,313,422,423,424,425,426,427,428,429,430,431),
        5 to listOf(501,502,503,504,505,506,507,508,509,510,511,512,513,514,217,515,516,517,415,518,519,520,521,522,523),
        6 to listOf(601,103,602,603,604,605,606,607,608,209,609,610,611,612,308,613,121,216,614,615,616,617,417,618,619,620,621,622,623,624,625,320),
        7 to listOf(701,702,703,704,705,706,707,708,607,709,311,213,710,308,122,512,711,712,218,713,413,714,715,716,717,718,719,720,721,722,723,724,725),
        8 to listOf(801,206,802,803,804,306,123,512,805,806,408,807,808,809,810,811,812,223,813,814,815,816,817,718,818,819)
    )

    /**
     * Compute first/last train times based on line position.
     * Terminal: 06:00/22:30, each station adds ~2.5 min.
     */
    private fun computeTrainTimes(stations: List<StationEntity>): Map<Int, Pair<String, String>> {
        val result = mutableMapOf<Int, Pair<String, String>>()
        for ((lineId, seq) in lineSequences) {
            val n = seq.size
            for ((idx, stationId) in seq.withIndex()) {
                val firstMinFromTerminal = (idx * 2.5).toInt()
                val lastMinFromTerminal = ((n - 1 - idx) * 2.5).toInt()
                val firstH = 6 + (firstMinFromTerminal / 60)
                val firstM = firstMinFromTerminal % 60
                val lastH = 22 + ((30 + lastMinFromTerminal) / 60)
                val lastM = (30 + lastMinFromTerminal) % 60
                val ft = "%02d:%02d".format(firstH % 24, firstM)
                val lt = "%02d:%02d".format(lastH % 24, lastM)
                result[stationId] = Pair(ft, lt)
            }
        }
        return result
    }

    private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun minutesFromKm(km: Double): Int = maxOf(2, (km * 60 / 35 + 1).toInt())

    fun getInitialConnections(): List<ConnectionEntity> {
        val stationMap = getInitialStations().associateBy { it.stationId }
        val result = mutableListOf<ConnectionEntity>()
        var connId = 0

        fun connect(from: Int, to: Int, weight: Int, lineId: Int) {
            result.add(ConnectionEntity(connId++, from, to, weight, lineId))
            result.add(ConnectionEntity(connId++, to, from, weight, lineId))
        }

        for ((lineId, seq) in lineSequences) {
            seq.zipWithNext { a, b ->
                val s1 = stationMap[a]!!
                val s2 = stationMap[b]!!
                val km = haversineKm(s1.latitude!!, s1.longitude!!, s2.latitude!!, s2.longitude!!)
                val w = minutesFromKm(km)
                connect(a, b, w, lineId)
            }
        }

        return result
    }
}
