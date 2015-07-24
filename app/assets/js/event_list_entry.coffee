define [
  'react'
  ], (React) ->
    EventListEntry = React.createClass
        render: ->
            { div, span, button } = React.DOM
            if @props.event
                eid = @props.event.id

                eventDate = new Date(@props.event.start)
                readableDate = eventDate.toDateString()

                orderButton = button { key: 'o' }, "Order"

                div {
                  key: "er-#{ eid }"
                  className: "eventEntry"
                }, [
                    span { key: 'evn' }, @props.event.name
                    span { key: 'evc' }, @props.event.city
                    span { key: 'evd' }, readableDate
                    span { key: 'order' }, orderButton
                ]
            else
                null

    EventListEntry
