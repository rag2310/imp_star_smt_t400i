package com.rago.imp_start_smt_t400i

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.starmicronics.stario.PortInfo
import com.starmicronics.stario.StarIOPort
import com.starmicronics.stario.StarIOPortException
import com.starmicronics.stario.StarPrinterStatus
import com.starmicronics.starioextension.ICommandBuilder
import com.starmicronics.starioextension.StarIoExt

class MainActivity : AppCompatActivity() {

    lateinit var port: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSearch: Button = findViewById(R.id.search_imp)
        val btnImpTest: Button = findViewById(R.id.imp_test)
        val portName: TextView = findViewById(R.id.port_name)
        val macAddress: TextView = findViewById(R.id.mac_address)
        val modelName: TextView = findViewById(R.id.model_name)

        btnImpTest.isEnabled = false

        btnSearch.setOnClickListener {
            try {
                val porList: List<PortInfo> = StarIOPort.searchPrinter("BT:")

                porList.forEach {
                    portName.text = it.portName
                    macAddress.text = it.macAddress
                    port = "BT:${it.macAddress}"
                    modelName.text = port
                }

                btnImpTest.isEnabled = true

            } catch (e: StarIOPortException) {

            }
        }



        btnImpTest.setOnClickListener {
            try {

                val starIOPort: StarIOPort = StarIOPort.getPort(port, "", 6000, baseContext)

                var status: StarPrinterStatus = starIOPort.beginCheckedBlock()

                System.out.println(status.toString())

                if (status.offline) {
                    System.out.println("OFFLINE")
                }

                val command: ByteArray = createData(StarIoExt.Emulation.StarPRNT)

                starIOPort.writePort(command, 0, command.size)

                status = starIOPort.endCheckedBlock()

                print("STATUS: $status")

                if (status.offline == false) {
                    print("success")
                } else {
                    print("fail")

                }

            } catch (e: StarIOPortException) {
                print("Error: $e")
            } finally {

            }
        }
    }

    fun createData(emulation: StarIoExt.Emulation): ByteArray {
        val data: ByteArray = "Hello World.\n".toByteArray()
        val builder: ICommandBuilder = StarIoExt.createCommandBuilder(emulation)

        val icon: Bitmap = BitmapFactory.decodeResource(baseContext.resources, R.drawable.logo)

        builder.beginDocument()
        builder.append(data)
        builder.appendBitmap(icon, false)
        builder.appendCutPaper(ICommandBuilder.CutPaperAction.PartialCutWithFeed)
        builder.endDocument()
        return builder.commands
    }
}