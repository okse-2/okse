/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Norwegian Defence Research Establishment / NTNU
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * Created by Kristoffer Dalby (kradalby) on 02/03/15.
 */

var Logs = (function($) {

    var url = "log?logLevel=DEBUG&logID=0"
    var logLevel = "DEBUG"
    var logID = 0
    var logLength = 250

    var updateUrl = function() {
        url = "log?logLevel=" + logLevel + "&logID=" + logID + "&length=" + logLength
    }

    var updateLogView = function(data) {
        var HTML = '<pre class="log-view">';
        $.each(data.lines, function (i, line) {
           HTML += line + '\n'
        });
        HTML += '</pre>'
        return HTML
    }

    return {
        init: function() {
            /*
            * Get log levels from the backend.
            * Create buttons
            * Initialize buttons with filters
            * */
            Main.ajax("log/levels", function(){
                console.log("[Debug][Logs] Failed to load log levels")
            }, function(data) {
                console.log("[Debug][Logs] Adding log level buttons")
                var buttons = ""
                $.each(data, function(i, level) {
                    buttons += '<div class="btn-group" role="group"><button type="button" class="btn btn-info" id="button-' + level + '">' + level + '</button></div>'
                });
                $("#log-level").html(buttons)
                $.each(data, function(i, level) {
                    $("#button-" + level).on("click", function(){
                        logLevel = level
                        updateUrl()
                        Main.ajax(Logs.url(), Logs.error, Logs.refresh, "GET")
                    })
                })
            }, "GET", "json")

            /*
             * Get log files from the backend.
             * Create buttons
             * Initialize buttons with filters
             * */
            Main.ajax("log/files", function(){
                console.log("[Debug][Logs] Failed to load log files")
            }, function(data) {
                console.log("[Debug][Logs] Adding log files buttons")
                var files = ""
                $.each(data, function(i) {
                    files += '<div class="btn-group" role="group"><button type="button" class="btn btn-success" id="button-logID-' + i + '">' + data[i] + '</button></div>'
                })
                $("#log-file").html(files)
                $.each(data, function(i) {
                    $("#button-logID-" + i).on("click", function(){
                        logID = i
                        updateUrl()
                        Main.ajax(Logs.url(), Logs.error, Logs.refresh, "GET")
                    })
                })
            }, "GET", "json")
            $("#log-length").keyup(function(){
                logLength = $(this).val()
                updateUrl()
                Main.ajax(Logs.url(), Logs.error, Logs.refresh, "GET")
            })

            /*
             * Add a listener to clear interval
             * */
            $("#button-refresh").on("click", function() {
                if (!$(this).hasClass("active")) {
                    $(this).addClass("active")
                    $(this).text("Start refresh")
                    Main.clearIntervalForTab()
                } else {
                    $(this).removeClass("active");
                    $(this).text("Stop refresh")
                    Main.setIntervalForLogTab()
                }
            })
        },
        error: function(xhr, status, error) {
            console.error("[Error][Logs] in Ajax with the following callback [status: " + xhr.status +  " readyState: " + xhr.readyState + " responseText: " + xhr.responseText + "]")
        },
        refresh: function(data) {
            $('#log-name').html(data.name)
            $('#log-data').html(updateLogView(data))
        },
        url: function() {
            return url
        }
    }

})(jQuery)
