package ru.tinkoff.codefest.http.telegram

import java.util.NoSuchElementException

import com.bot4s.telegram.methods.Response
import com.bot4s.telegram.methods.ChatAction.ChatAction
import com.bot4s.telegram.methods.ParseMode.ParseMode
import com.bot4s.telegram.models._
import com.bot4s.telegram.methods.{ChatAction, ParseMode}
import com.bot4s.telegram.models.ChatType.ChatType
import com.bot4s.telegram.models.CountryCode.CountryCode
import com.bot4s.telegram.models.Currency.Currency
import com.bot4s.telegram.models.MaskPositionType.MaskPositionType
import com.bot4s.telegram.models.MemberStatus.MemberStatus
import com.bot4s.telegram.models.MessageEntityType.MessageEntityType
import UpdateType.UpdateType
import com.bot4s.telegram.marshalling.CaseConversions
import com.bot4s.telegram.models._
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import slogging.StrictLogging

/** Circe marshalling borrowed/inspired from [[https://github.com/nikdon/telepooz]]
  */
trait CirceDecoders extends StrictLogging with CaseConversions {

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val memberStatusDecoder: Decoder[MemberStatus] =
    Decoder[String].map(s => MemberStatus.withName(pascalize(s)))
  implicit val maskPositionTypeDecoder: Decoder[MaskPositionType] =
    Decoder[String].map(s => MaskPositionType.withName(pascalize(s)))

  implicit val chatTypeDecoder: Decoder[ChatType] =
    Decoder[String].map(s => ChatType.withName(pascalize(s)))

  implicit val messageEntityTypeDecoder: Decoder[MessageEntityType] =
    Decoder[String].map { s =>
      try {
        MessageEntityType.withName(pascalize(s))
      } catch {
        case e: NoSuchElementException =>
          logger.warn(s"Unexpected MessageEntityType: '$s', fallback to Unknown.")
          MessageEntityType.Unknown
      }
    }

  implicit val parseModeDecoder: Decoder[ParseMode] =
    Decoder[String].map(s => ParseMode.withName(pascalize(s)))

  implicit val countryCodeDecoder: Decoder[CountryCode] =
    Decoder[String].map(a => CountryCode.withName(a))

  implicit val currencyDecoder: Decoder[Currency] =
    Decoder[String].map(a => Currency.withName(a))

  implicit val chatIdDecoder: Decoder[ChatId] =
    Decoder[String].map(ChatId.Channel) or Decoder[Long].map(ChatId.Chat)

  implicit val chatActionDecoder: Decoder[ChatAction] =
    Decoder[String].map(s => ChatAction.withName(pascalize(s)))

  implicit val updateTypeDecoder: Decoder[UpdateType] =
    Decoder[String].map(s => UpdateType.withName(pascalize(s)))

  implicit val audioDecoder: Decoder[Audio] = deriveDecoder[Audio]

  implicit val chatDecoder: Decoder[Chat] = deriveDecoder[Chat]
  implicit val chatPhotoDecoder: Decoder[ChatPhoto] = deriveDecoder[ChatPhoto]

  implicit val contactDecoder: Decoder[Contact] = deriveDecoder[Contact]
  implicit val documentDecoder: Decoder[Document] = deriveDecoder[Document]
  implicit val fileDecoder: Decoder[File] = deriveDecoder[File]
  implicit val callbackGameDecoder: Decoder[CallbackGame] = deriveDecoder[CallbackGame]
  implicit val inlineKeyboardButtonDecoder: Decoder[InlineKeyboardButton] =
    deriveDecoder[InlineKeyboardButton]
  implicit val keyboardButtonDecoder: Decoder[KeyboardButton] = deriveDecoder[KeyboardButton]
  implicit val locationDecoder: Decoder[Location] = deriveDecoder[Location]

  implicit val messageEntityDecoder: Decoder[MessageEntity] = deriveDecoder[MessageEntity]

  implicit val webhookInfoDecoder: Decoder[WebhookInfo] = deriveDecoder[WebhookInfo]

  implicit val photoSizeDecoder: Decoder[PhotoSize] = deriveDecoder[PhotoSize]

  implicit val replyMarkupDecoder: Decoder[ReplyMarkup] = deriveDecoder[ReplyMarkup]

  implicit val stickerDecoder: Decoder[Sticker] = deriveDecoder[Sticker]

  implicit val messageDecoder: Decoder[Message] = deriveDecoder[Message]
  implicit val callbackQueryDecoder: Decoder[CallbackQuery] = deriveDecoder[CallbackQuery]

  implicit val stickerSetDecoder: Decoder[StickerSet] = deriveDecoder[StickerSet]

  implicit val chatMemberDecoder: Decoder[ChatMember] = deriveDecoder[ChatMember]

  implicit val maskPositionDecoder: Decoder[MaskPosition] = deriveDecoder[MaskPosition]

  implicit val userDecoder: Decoder[User] = deriveDecoder[User]
  implicit val userProfilePhotosDecoder: Decoder[UserProfilePhotos] =
    deriveDecoder[UserProfilePhotos]
  implicit val venueDecoder: Decoder[Venue] = deriveDecoder[Venue]
  implicit val videoDecoder: Decoder[Video] = deriveDecoder[Video]
  implicit val videoNoteDecoder: Decoder[VideoNote] = deriveDecoder[VideoNote]
  implicit val voiceDecoder: Decoder[Voice] = deriveDecoder[Voice]

  implicit val gameHighScoreDecoder: Decoder[GameHighScore] = deriveDecoder[GameHighScore]
  implicit val animationDecoder: Decoder[Animation] = deriveDecoder[Animation]
  implicit val gameDecoder: Decoder[Game] = deriveDecoder[Game]

  implicit val inlineQueryDecoder: Decoder[InlineQuery] = deriveDecoder[InlineQuery]
  implicit val chosenInlineQueryDecoder: Decoder[ChosenInlineResult] =
    deriveDecoder[ChosenInlineResult]

  implicit val inputContactMessageContent: Decoder[InputContactMessageContent] =
    deriveDecoder[InputContactMessageContent]
  implicit val inputVenueMessageContentDecoder: Decoder[InputVenueMessageContent] =
    deriveDecoder[InputVenueMessageContent]
  implicit val inputLocationMessageContentDecoder: Decoder[InputLocationMessageContent] =
    deriveDecoder[InputLocationMessageContent]
  implicit val inputTextMessageContentDecoder: Decoder[InputTextMessageContent] =
    deriveDecoder[InputTextMessageContent]

  implicit val labeledPriceDecoder: Decoder[LabeledPrice] = deriveDecoder[LabeledPrice]
  implicit val invoiceDecoder: Decoder[Invoice] = deriveDecoder[Invoice]
  implicit val shippingAddressDecoder: Decoder[ShippingAddress] = deriveDecoder[ShippingAddress]

  implicit val shippingQueryDecoder: Decoder[ShippingQuery] = deriveDecoder[ShippingQuery]
  implicit val orderInfoDecoder: Decoder[OrderInfo] = deriveDecoder[OrderInfo]
  implicit val preCheckoutQueryDecoder: Decoder[PreCheckoutQuery] = deriveDecoder[PreCheckoutQuery]
  implicit val shippingOptionDecoder: Decoder[ShippingOption] = deriveDecoder[ShippingOption]
  implicit val successfulPaymentDecoder: Decoder[SuccessfulPayment] =
    deriveDecoder[SuccessfulPayment]

  implicit val responseParametersDecoder: Decoder[ResponseParameters] =
    deriveDecoder[ResponseParameters]

  implicit val updateDecoder: Decoder[Update] = deriveDecoder[Update]

  implicit def responseDecoder[T](implicit decT: Decoder[T]): Decoder[Response[T]] =
    deriveDecoder[Response[T]]

  implicit def eitherDecoder[A, B](
      implicit decA: Decoder[A],
      decB: Decoder[B]
  ): Decoder[Either[A, B]] = {
    val l: Decoder[Either[A, B]] = decA.map(Left.apply)
    val r: Decoder[Either[A, B]] = decB.map(Right.apply)
    l or r
  }
}

object CirceDecoders extends CirceDecoders
