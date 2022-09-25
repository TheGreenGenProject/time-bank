package org.timebank.service.search

import org.timebank.core.Location.PostCode
import org.timebank.core.{Activity, ActivityId, Hashtag, Page, TimeSlot}


trait SearchService[M[_]] {

  def index(activityId: ActivityId,
            hashtags: Set[Hashtag])

  def search(hashtags: Set[Hashtag],
             page: Page): M[List[Activity]]

  def search(hashtags: List[Hashtag],
             timeSlot: List[TimeSlot],
             page: Page): M[List[Activity]]

  def searchByPostCodes(hashtags: List[Hashtag],
                        timeSlot: List[TimeSlot],
                        postCodes: List[PostCode],
                        page: Page): M[List[Activity]]

  def searchOnline(hashtags: List[Hashtag],
                   timeSlot: List[TimeSlot],
                   page: Page): M[List[Activity]]

  def hashtags(prefix: String,
               page: Page): M[List[Hashtag]]

  def topOffering(n: Int): Map[Hashtag, Int]

  def topSearch(n: Int): Map[Hashtag, Int]

}
