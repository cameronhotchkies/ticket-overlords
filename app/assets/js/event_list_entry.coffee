define [
  'react'
  ], (React) ->
    EventListEntry = React.createClass
        getInitialState: ->
            expanded: false

        toggleExpanded: ->
            @setState 
                expanded: !@state.expanded

        renderEntryBlocks: ->
            { div, span } = React.DOM
            
            div { key: 'blocks', className: 'blocks' },
                span { key: 'ph' }, "Placeholder"
            
        render: ->
            { div, span, button } = React.DOM
            if @props.event?
                eid = @props.event.id

                eventDate = new Date(@props.event.start)
                readableDate = eventDate.toDateString()
                
                orderText = if @state.expanded then "Cancel" else "Order"
                orderButton = button {
                    key: 'o'
                    onClick: @toggleExpanded
                }, orderText

                baseRow = div {
                  key: "er-#{ eid }"
                  className: "eventEntry"
                }, [
                    span { key: 'evn' }, @props.event.name
                    span { key: 'evc' }, @props.event.city
                    span { key: 'evd' }, readableDate
                    span { key: 'order' }, orderButton
                ]
                
                contents = [baseRow]

                if @state.expanded
                    contents.push @renderEntryBlocks()

                div {}, contents
            else
                null

    EventListEntry
