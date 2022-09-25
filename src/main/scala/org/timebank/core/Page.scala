package org.timebank.core

case class Page(number: Int, size: Int)

object Page {

  // To return all results with a paginated request
  val All = Page(1, Int.MaxValue)
  val DefaultPageSIze = 50

  def get[T](page: Page, xs: List[T]): List[T] =
    xs.slice(page.number * page.size, page.number * page.size + page.size)
}
