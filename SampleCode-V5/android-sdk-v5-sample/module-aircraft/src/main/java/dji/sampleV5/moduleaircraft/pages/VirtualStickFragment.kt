package dji.sampleV5.moduleaircraft.pages

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import dji.sampleV5.moduleaircraft.R
import dji.sampleV5.moduleaircraft.models.BasicAircraftControlVM
import dji.sampleV5.moduleaircraft.models.SimulatorVM
import dji.sampleV5.moduleaircraft.models.VirtualStickVM
import dji.sampleV5.modulecommon.keyvalue.KeyValueDialogUtil
import dji.sampleV5.modulecommon.pages.DJIFragment
import dji.sampleV5.modulecommon.util.Helper
import dji.sdk.keyvalue.value.common.EmptyMsg
import dji.sdk.keyvalue.value.common.LocationCoordinate2D
import dji.sdk.keyvalue.value.flightcontroller.VirtualStickFlightControlParam
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.manager.aircraft.simulator.InitializationSettings
import dji.v5.manager.aircraft.virtualstick.Stick
import dji.v5.utils.common.JsonUtil
import dji.v5.utils.common.ToastUtils
import kotlinx.android.synthetic.main.frag_simulator_page.simulator_state_info_tv
import kotlinx.android.synthetic.main.frag_virtual_stick_page.*

/**
 * Class Description
 *
 * @author Hoker
 * @date 2021/5/11
 *
 * Copyright (c) 2021, DJI All Rights Reserved.
 */
class VirtualStickFragment : DJIFragment() {

    private val basicAircraftControlVM: BasicAircraftControlVM by activityViewModels()
    private val virtualStickVM: VirtualStickVM by activityViewModels()
    private val simulatorVM: SimulatorVM by activityViewModels()
    private val deviation: Double = 0.02

    private val handler: Handler = Handler(Looper.getMainLooper())


    private val coordinate2D = LocationCoordinate2D(22.5797650, 113.941171)
    private val data = InitializationSettings.createInstance(coordinate2D, 15)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_virtual_stick_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        widget_horizontal_situation_indicator.setSimpleModeEnable(false)
        initBtnClickListener()
//        initStickListener()
        virtualStickVM.listenRCStick()
        virtualStickVM.currentSpeedLevel.observe(viewLifecycleOwner) {
            updateVirtualStickInfo()
        }
        virtualStickVM.useRcStick.observe(viewLifecycleOwner) {
            updateVirtualStickInfo()
        }
        virtualStickVM.currentVirtualStickStateInfo.observe(viewLifecycleOwner) {
            updateVirtualStickInfo()
        }
        virtualStickVM.stickValue.observe(viewLifecycleOwner) {
            updateVirtualStickInfo()
        }
        virtualStickVM.virtualStickAdvancedParam.observe(viewLifecycleOwner) {
            updateVirtualStickInfo()
        }
        simulatorVM.simulatorStateSb.observe(viewLifecycleOwner) {
            simulator_state_info_tv.text = it
        }
    }

    private fun initBtnClickListener() {
        btn_enable_virtual_stick.setOnClickListener {
            virtualStickVM.enableVirtualStick(object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    ToastUtils.showToast("enableVirtualStick success.")
                }

                override fun onFailure(error: IDJIError) {
                    ToastUtils.showToast("enableVirtualStick error,$error")
                }
            })
        }

        btn_disable_virtual_stick.setOnClickListener {
            virtualStickVM.disableVirtualStick(object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    ToastUtils.showToast("disableVirtualStick success.")
                }

                override fun onFailure(error: IDJIError) {
                    ToastUtils.showToast("disableVirtualStick error,${error})")
                }
            })
        }

        btn_enable_simulator.setOnClickListener {
            simulatorVM.enableSimulator(data, object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    ToastUtils.showToast("enable simulator success")
                }

                override fun onFailure(error: IDJIError) {
                    ToastUtils.showToast("enable simulator Failed" + error.description())
                }

            })
        }

        btn_disable_simulator.setOnClickListener {
            simulatorVM.disableSimulator(object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    ToastUtils.showToast("disable simulator success")
                }

                override fun onFailure(error: IDJIError) {
                    ToastUtils.showToast("disable simulator Failed" + error.description())
                }
            })
        }
        btn_set_virtual_stick_speed_level.setOnClickListener {
            val speedLevels = doubleArrayOf(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0)
            initPopupNumberPicker(Helper.makeList(speedLevels)) {
                virtualStickVM.setSpeedLevel(speedLevels[indexChosen[0]])
                resetIndex()
            }
        }
        btn_square.setOnClickListener {
            class GotoPos(
                val launch_message: String,
                val right_horizontal: Float,
                val right_vertical: Float
            ) : Runnable {
                override fun run() {
                    if (launch_message != "")
                        ToastUtils.showToast(launch_message)
                    virtualStickVM.setRightPosition(
                        (right_horizontal * Stick.MAX_STICK_POSITION_ABS).toInt(),
                        (right_vertical * Stick.MAX_STICK_POSITION_ABS).toInt()
                    )
                }
            }
            ToastUtils.showToast("Takeoff")
            basicAircraftControlVM.startTakeOff(object :
                CommonCallbacks.CompletionCallbackWithParam<EmptyMsg> {
                override fun onSuccess(t: EmptyMsg?) {
                    ToastUtils.showToast("start takeOff onSuccess.")
                }

                override fun onFailure(error: IDJIError) {
                    ToastUtils.showToast("start takeOff onFailure,$error")
                }
            })
            //Goto Pos1
            handler.postDelayed(GotoPos("Go to Position1", 0f, -0.1f), 4000)
            handler.postDelayed(GotoPos("", 0f, 0.01f), 8000)
            handler.postDelayed(GotoPos("", 0f, 0f), 8100)

            //Goto Pos2
            handler.postDelayed(GotoPos("Go to Position2", -0.1f, 0f), 8100)
            handler.postDelayed(GotoPos("", 0.01f, 0f), 12100)
            handler.postDelayed(GotoPos("", 0f, 0f), 12200)

            //Goto Pos3
            handler.postDelayed(GotoPos("Go to Position3", 0f, 0.1f), 12200)
            handler.postDelayed(GotoPos("", 0f, -0.01f), 20200)
            handler.postDelayed(GotoPos("", 0f, 0f), 20300)

            //Goto Pos4
            handler.postDelayed(GotoPos("Go to Position4", 0.1f, 0f), 20300)
            handler.postDelayed(GotoPos("", -0.01f, 0f), 28300)
            handler.postDelayed(GotoPos("", 0f, 0f), 28400)

            //Goto Pos5
            handler.postDelayed(GotoPos("Go to Position5", 0f, -0.1f), 28400)
            handler.postDelayed(GotoPos("", 0f, 0.01f), 36400)
            handler.postDelayed(GotoPos("", 0f, 0f), 36500)

            //Goto Pos1
            handler.postDelayed(GotoPos("Go to Position1", -0.1f, 0f), 36500)
            handler.postDelayed(GotoPos("", 0.01f, 0f), 40500)
            handler.postDelayed(GotoPos("", 0f, 0f), 40600)

            //Goto Home
            handler.postDelayed(GotoPos("Go to Home", 0f, 0.1f), 40600)
            handler.postDelayed(GotoPos("", 0f, -0.01f), 44600)
            handler.postDelayed(GotoPos("", 0f, 0f), 44700)

            //Landing
            handler.postDelayed(
                object : Runnable {
                    override fun run() {
                        basicAircraftControlVM.startLanding(object :
                            CommonCallbacks.CompletionCallbackWithParam<EmptyMsg> {
                            override fun onSuccess(t: EmptyMsg?) {
                                ToastUtils.showToast("start landing onSuccess.")
                            }

                            override fun onFailure(error: IDJIError) {
                                ToastUtils.showToast("start landing onFailure,$error")
                            }
                        })
                    }
                },
                48600)
        }
        btn_take_off.setOnClickListener {
            basicAircraftControlVM.startTakeOff(object :
                CommonCallbacks.CompletionCallbackWithParam<EmptyMsg> {
                override fun onSuccess(t: EmptyMsg?) {
                    ToastUtils.showToast("start takeOff onSuccess.")
                    ToastUtils.showToast("any more?") // 文字列の表示
                }

                override fun onFailure(error: IDJIError) {
                    ToastUtils.showToast("start takeOff onFailure,$error")
                }
            })
        }
        btn_landing.setOnClickListener {
            basicAircraftControlVM.startLanding(object :
                CommonCallbacks.CompletionCallbackWithParam<EmptyMsg> {
                override fun onSuccess(t: EmptyMsg?) {
                    ToastUtils.showToast("start landing onSuccess.")
                }

                override fun onFailure(error: IDJIError) {
                    ToastUtils.showToast("start landing onFailure,$error")
                }
            })
        }
        btn_use_rc_stick.setOnClickListener {
            virtualStickVM.useRcStick.value = virtualStickVM.useRcStick.value != true
            if (virtualStickVM.useRcStick.value == true) {
                ToastUtils.showToast(
                    "After it is turned on," +
                            "the joystick value of the RC will be used as the left/ right stick value"
                )
            }
        }
        btn_set_virtual_stick_advanced_param.setOnClickListener {
            KeyValueDialogUtil.showInputDialog(
                activity, "Set Virtual Stick Advanced Param",
                JsonUtil.toJson(virtualStickVM.virtualStickAdvancedParam.value), "", false
            ) {
                it?.apply {
                    val param = JsonUtil.toBean(this, VirtualStickFlightControlParam::class.java)
                    if (param == null) {
                        ToastUtils.showToast("Value Parse Error")
                        return@showInputDialog
                    }
                    virtualStickVM.virtualStickAdvancedParam.postValue(param)
                }
            }
        }
        btn_send_virtual_stick_advanced_param.setOnClickListener {
            virtualStickVM.virtualStickAdvancedParam.value?.let {
                virtualStickVM.sendVirtualStickAdvancedParam(it)
            }
        }
        btn_enable_virtual_stick_advanced_mode.setOnClickListener {
            virtualStickVM.enableVirtualStickAdvancedMode()
        }
        btn_disable_virtual_stick_advanced_mode.setOnClickListener {
            virtualStickVM.disableVirtualStickAdvancedMode()
        }
    }

//    private fun initStickListener() {
//        left_stick_view.setJoystickListener(object : OnScreenJoystickListener {
//            override fun onTouch(joystick: OnScreenJoystick?, pX: Float, pY: Float) {
//                var leftPx = 0F
//                var leftPy = 0F
//
//                if (abs(pX) >= deviation) {
//                    leftPx = pX
//                }
//
//                if (abs(pY) >= deviation) {
//                    leftPy = pY
//                }
//
//                virtualStickVM.setLeftPosition(
//                    (leftPx * Stick.MAX_STICK_POSITION_ABS).toInt(),
//                    (leftPy * Stick.MAX_STICK_POSITION_ABS).toInt()
//                )
//            }
//        })
//        right_stick_view.setJoystickListener(object : OnScreenJoystickListener {
//            override fun onTouch(joystick: OnScreenJoystick?, pX: Float, pY: Float) {
//                var rightPx = 0F
//                var rightPy = 0F
//
//                if (abs(pX) >= deviation) {
//                    rightPx = pX
//                }
//
//                if (abs(pY) >= deviation) {
//                    rightPy = pY
//                }
//
//                virtualStickVM.setRightPosition(
//                    (rightPx * Stick.MAX_STICK_POSITION_ABS).toInt(),
//                    (rightPy * Stick.MAX_STICK_POSITION_ABS).toInt()
//                )
//            }
//        })
//    }

    private fun updateVirtualStickInfo() {
        val builder = StringBuilder()
        builder.append("Speed level:").append(virtualStickVM.currentSpeedLevel.value)
        builder.append("\n")
        builder.append("Use rc stick as virtual stick:").append(virtualStickVM.useRcStick.value)
        builder.append("\n")
        builder.append("Is virtual stick enable:")
            .append(virtualStickVM.currentVirtualStickStateInfo.value?.state?.isVirtualStickEnable)
        builder.append("\n")
        builder.append("Current control permission owner:")
            .append(virtualStickVM.currentVirtualStickStateInfo.value?.state?.currentFlightControlAuthorityOwner)
        builder.append("\n")
        builder.append("Change reason:")
            .append(virtualStickVM.currentVirtualStickStateInfo.value?.reason)
        builder.append("\n")
        builder.append("Rc stick value:").append(virtualStickVM.stickValue.value?.toString())
        builder.append("\n")
        builder.append("Is virtual stick advanced mode enable:")
            .append(virtualStickVM.currentVirtualStickStateInfo.value?.state?.isVirtualStickAdvancedModeEnabled)
        builder.append("\n")
        builder.append("Virtual stick advanced mode param:")
            .append(virtualStickVM.virtualStickAdvancedParam.value?.toJson())
        builder.append("\n")
        mainHandler.post {
            virtual_stick_info_tv.text = builder.toString()
        }
    }
}