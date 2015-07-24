define [
  'react'
  'jquery'
  ], (React, jQuery) ->

    EventList = React.createClass
        getInitialState: ->
            hasLoaded: false
            events: []

        componentDidMount: ->
            eventListApi = jsRoutes.controllers.Events.list()
            eventListApi.ajax()
              .done (result) =>
                  if @isMounted()
                      @setState
                        events: result.response
                        hasLoaded: true
              .fail (jqXHR, textStatus, errorThrown) =>
                  resultCode = jqXHR.status
                  if @isMounted()
                      @setState
                        events: []
                        hasLoaded: true

        render: ->
            # TODO

    EventList
