package lambdas.database

import org.scalatest._
import org.scalamock.scalatest.{AsyncMockFactory, MockFactory}
import awscala._
import cats.effect.IO
import dynamodbv2._
import org.scalacheck._
import lambdas.database._
import lambdas.config
import lambdas.config.AWSConfig
import lambdas.config

class DatabaseTest extends FunSpec with Matchers with MockFactory {

  describe("Database Tests") {
      describe("AccessKeys") {
          it("Access Key Returns Correct Region for US-EAST-1") {
            val testAccessString = Gen.alphaNumChar.toString
            val testSecretKey = Gen.alphaNumChar.toString
            val testRegion = "US-EAST-1"
            val accessKey = new AwsAccessKeys(new AWSConfig(testAccessString, testSecretKey, testRegion))
            assert(accessKey.getRegion.equals(Region.US_EAST_1))
          }
      }

      describe("DynamoDBProxy") {
          it("AwsDynamoProxy apply companion object should return an instance of AwsDynamoProxy") {
            assert(ops.AwsDynamoProxy.equals(AwsDynamoProxy(ops.accessKeys, ops.fakeTableName)))
          }

          it("AwsDynamoProxy put given a primaryKey and a sequence of attributes should return an IO") {
            val dynamoTableStub = stub[Table]
            val dynamoStub = stub[DynamoDB]

            val accessKey = Gen.alphaNumChar.toString
            val secreteAccessKey = Gen.alphaNumChar.toString
            val testRegion = Gen.alphaNumChar.toString
            val testTableName = Gen.alphaNumChar.toString
            val testAwsConfig = new AWSConfig(accessKey, secreteAccessKey, testRegion)
            val testAccessKeys = new AwsAccessKeys(testAwsConfig)
            val testSequence : Seq[(String, Any)] = (1 to 10).map((n: Int) => (Gen.alphaNumChar.toString, Gen.alphaNumChar.toString))
            val testPrimaryKey: String = Gen.alphaNumChar.toString

            (dynamoTableStub.putAttributes (_: Any, _:Seq[(String, Any)])(_: DynamoDB))
                .when(testPrimaryKey, testSequence, dynamoStub)
                .returning(Unit)

            (dynamoStub.table (_: String)).when(testTableName).returning(Some(dynamoTableStub))

            val testAwsDynamoProxy = new AwsDynamoProxy(testAccessKeys, testTableName) {
                override def getTable(dynamo: DynamoDB, table: String) = dynamoTableStub
            }
            assert(testAwsDynamoProxy.put(testPrimaryKey, testSequence).isInstanceOf[IO[Unit]])
          }
      }
  }

  object ops {
    val testAccessString = Gen.alphaNumChar.toString
    val testSecretKey = Gen.alphaNumChar.toString
    val fakeTableName = Gen.alphaNumChar.toString
    val accessKeys: AwsAccessKeys = new AwsAccessKeys(new AWSConfig(testAccessString, testSecretKey, "US-EAST-1"))
    val AwsDynamoProxy: AwsDynamoProxy = new AwsDynamoProxy(accessKeys, fakeTableName)
  }
}
