package org.timebank.service.search

import org.timebank.core.Location.PostCode
import org.timebank.core.{Activity, Hashtag, Page, TimeSlot}


trait SearchService[M[_]] {

  def search(hashtag: List[Hashtag]): M[List[Activity]]

  def search(hashtag: List[Hashtag], timeSlot: List[TimeSlot]): M[List[Activity]]

  def searchByPostCodes(hashtag: List[Hashtag], timeSlot: List[TimeSlot], postCodes: List[PostCode]): M[List[Activity]]

  def searchOnline(hashtag: List[Hashtag], timeSlot: List[TimeSlot]): M[List[Activity]]

  def hashtags(prefix: String, page: Page): M[List[Hashtag]]

  def topOffering(n: Int): Map[Hashtag, Int]

  def topSearch(n: Int): Map[Hashtag, Int]

}
