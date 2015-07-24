define [
  'react'
  'jquery'
  'event_list_entry'
  ], (React, jQuery, EventListEntry) ->

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
            { div } = React.DOM
            if @state.hasLoaded
                eventListEntry = React.createFactory EventListEntry
                
                entries = @state.events.map (event) ->
                    eventListEntry
                        key: event.id
                        event: event
                
                div {
                    key: 'el'
                    className: 'eventEntries'
                },  entries
            else
                div {}

    EventList
