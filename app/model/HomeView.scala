/**
 * to do sample project
 */

package model

import lib.model.Todo

// Topページのviewvalue
case class HomeView(
    title: String,
    cssSrc: Seq[String],
    jsSrc: Seq[String]
) extends CommonView
