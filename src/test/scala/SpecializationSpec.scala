import java.util.UUID

import helpers.TestSpecializer
import io.legs.Specialization.Yield
import io.legs.{Specialization, Step}
import org.scalatest.FunSpec
import org.scalatest.concurrent.AsyncAssertions
import play.api.libs.json.{JsNumber, JsString}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class SpecializationSpec extends FunSpec with AsyncAssertions {

	it("intercepts routes with a parameter"){
		val uuid = UUID.randomUUID().toString
		Specialization.executeStep(Step("helpers.TestSpecializer/incr/key", Some(Map("key" -> JsString(uuid))),None), Map())
		Specialization.executeStep(Step("helpers.TestSpecializer/incr/key", Some(Map("key" -> JsString(uuid))),None), Map())
		Specialization.executeStep(Step("helpers.TestSpecializer/incr/key", Some(Map("key" -> JsString(uuid))),None), Map())
		Specialization.executeStep(Step("helpers.TestSpecializer/incr/key", Some(Map("key" -> JsString(uuid))),None), Map()).onFailure {
			case (e)=> println("<<<<>>>",e)
		}
		assertResult(4) { TestSpecializer.getKeyValue(uuid) }
	}

	it("yields"){
		val res = Await.result(Specialization.executeStep(
			Step("helpers.TestSpecializer/incrState/from",None,Some("ztarget")),
			Map("from" -> 20)
		), Duration("5 seconds"))

		assertResult( Yield(Some(21)) ) { res }

	}

	it ("does not yield when not specified"){
		val res = Await.result(Specialization.executeStep(
			Step("helpers.TestSpecializer/incrState/from",None,None),
			Map("from" -> 20)
		), Duration("5 seconds"))

		assertResult( Yield(Some(21)) ) { res }

	}

	it("allows routes with just method names instead of full package path"){
		Await.result(Specialization.executeStep(
			Step("GENERATE/start/end",None,None),
			Map("start"-> JsNumber(20),"end"->JsNumber(22))
		),Duration("5 seconds")) match {
			case Yield(result) =>
				assertResult(result){ Some(List(20, 21)) }
			case bad =>
				fail("bad result")
		}
	}

}
