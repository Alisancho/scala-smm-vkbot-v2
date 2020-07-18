package core.onetaskwoman

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import ru.MyContext
import ru.database.Serially
import ru.database.query.QueryBlackListFriend

import scala.concurrent.duration._
class StreemTest extends FlatSpec with MockFactory with ScalaFutures with Matchers with QueryBlackListFriend with Serially {
  implicit val system             = ActorSystem()
  implicit val ec                 = system.dispatcher
  implicit val materializer       = ActorMaterializer()
  implicit val timeout: Timeout   = Timeout(60 seconds)
  implicit val oo: PatienceConfig = PatienceConfig(60 second) //Нужен для  Future
  implicit val ctx                = new MyContext
  behavior of "Test Proxy server IP"

  it should "with  ClassProxy" in {
//    def newFunGetBlack: Int => BlackListFriend =
//      BlackListFriend(namelist = "1ledon", _, idwoman = 475163284, datatime = Instant.now(), reason = "SEARCH")
//
//    val lickNain
//      : List[Int] = 5469661 :: 8595231 :: 8691922 :: 14956345 :: 15686794 :: 6446425 :: 7852953 :: 7890740 :: 85682434 :: 95220902 :: 98337777 :: 10011364 :: 10012689 :: 10133293 :: 136625855 :: Nil
//    //  MainFunction.getFilterList(lickNain)
//
//    // val popop = serially(lickNain)(getInfoBlackList)
//
//    val powww = MainFunction
//      .getFilterList[BlackListFriend](lickNain)(getInfoBlackList, newFunGetBlack)
//      .map(println)
////      .map(_.map(_ match {
////        case w: BlackListFriend => w.idman
////        case _                  => 0
////      }).filter(z ⇒ z != 0).toList)
////      .map(println)
//    Await.result(powww, Duration.Inf)
//    println("wdwdwdwdwdwd")
//    // Await.result(powww, Duration.Inf)
  }
}
