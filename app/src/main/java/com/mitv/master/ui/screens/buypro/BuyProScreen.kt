package com.mitv.master.ui.screens.buypro

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitv.master.ui.theme.MitvRed
import com.mitv.master.ui.theme.MitvSurface
import com.mitv.master.ui.theme.MitvTextSecondary

const val MITV_PRO_PRICE = "Rs 50 / month"
const val MITV_JAZZCASH_NUMBER = "03062015326"
const val MITV_WHATSAPP_NUMBER = "923062015326" // international format for wa.me link

/**
 * Guides the user through buying Pro: send Rs 50 via JazzCash, then send
 * the payment screenshot + their Gmail to WhatsApp so the admin can
 * manually activate Pro from the HTML admin panel.
 */
@Composable
fun BuyProScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Get MITV Pro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MitvSurface)
                .padding(20.dp)
        ) {
            Text(text = MITV_PRO_PRICE, color = MitvRed, fontWeight = FontWeight.Black, fontSize = 28.sp)
            Text(
                text = "4000+ Live Channels, Movies & Series",
                color = MitvTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            StepRow(number = "1", text = "Send $MITV_PRO_PRICE via JazzCash to:")
            InfoPill(text = MITV_JAZZCASH_NUMBER, context = context)

            StepRow(number = "2", text = "Take a screenshot of the payment")

            StepRow(number = "3", text = "Send the screenshot + your Gmail on WhatsApp:")
            InfoPill(text = MITV_JAZZCASH_NUMBER, context = context)

            StepRow(number = "4", text = "Pro will be activated on your account shortly")

            Button(
                onClick = {
                    val message = Uri.encode("Hi, I want to activate MITV Pro. Here is my payment screenshot and Gmail.")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$MITV_WHATSAPP_NUMBER?text=$message"))
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MitvRed),
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 16.dp)
            ) {
                Text("Open WhatsApp", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }

        Text(
            text = "💛 Support Muslim Islam Org — donations welcome on the same JazzCash number.",
            color = MitvTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}

@Composable
private fun StepRow(number: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
        Text(
            text = number,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.clip(RoundedCornerShape(50)).background(MitvRed).padding(horizontal = 8.dp, vertical = 2.dp)
        )
        Text(text = text, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun InfoPill(text: String, context: Context) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 26.dp, top = 4.dp, bottom = 4.dp)
            .clickable {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("JazzCash number", text))
                Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
            }
    ) {
        Text(text = text, color = MitvRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Icon(
            Icons.Filled.ContentCopy,
            contentDescription = "Copy",
            tint = MitvTextSecondary,
            modifier = Modifier.padding(start = 8.dp).height(16.dp)
        )
    }
}
