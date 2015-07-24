define [
  'react'
  ], (React) ->
    EventListEntry = React.createClass
        getInitialState: ->
            expanded: false
        
        gatherTicketBlocks: ->
            ticketBlocksApi = jsRoutes.controllers.Events.ticketBlocksForEvent(
                @props.event.id
            )
            ticketBlocksApi.ajax()
              .done (result) =>
                  if @isMounted()
                      availBlocks = (tb for tb in \
                          result.response when tb.availability > 0)
                      @setState
                          ticketBlocks: availBlocks
                          
              .fail (jqXHR, textStatus, errorThrown) =>
                  resultCode = jqXHR.status
                  if @isMounted()
                      @setState
                          ticketBlocks: null
        
        toggleExpanded: ->
            if @state.ticketBlocks == undefined
                @gatherTicketBlocks()
                @setState
                    ticketBlocks: null
            
            @setState
                expanded: !@state.expanded
        
        placeOrder: ->
            # TODO
        
        renderEntryBlocks: ->
            { div, span, option, label, select, input, button } = React.DOM
            eid = @props.event.id
            if @state.ticketBlocks?
                if @state.ticketBlocks.length > 0
                      options = @state.ticketBlocks.map (tb) ->
                          priceFormat = parseFloat(Math.round(tb.price * 100) / 100).toFixed(2)
                          option {
                              key: tb.id
                              value: tb.id
                          }, "#{ tb.name } - $#{ priceFormat }"
                          
                      blockChoice = select {
                          key: 'tbo'
                          id: "tbo#{eid}"
                      }, options
                      
                      div { key: 'opnl' }, [
                          div { key: 'q'}, [
                              label {
                                  key: 'lt'
                                  htmlFor: "tbo#{eid}"
                              }, "Tickets:"
                              blockChoice
                              
                              label {
                                  key: 'lq'
                                  htmlFor: "qty#{eid}"
                              }, "Quantity:"
                              input {
                                  key: 'qty'
                                  id: "qty#{eid}"
                                  type: "number"
                                  max: 9999
                                  min: 1
                                  defaultValue: 1
                              }
                          ],
                          
                          div { key: 'n' }, [
                              label {
                                  key: 'ln'
                                  htmlFor: "name#{eid}"
                              }, "Name:"
                              input {
                                  key: 'name'
                                  id: "name#{eid}"
                              }
                              
                              label {
                                  key: 'le'
                                  htmlFor: "email#{eid}"
                              }, "Email:"
                              input {
                                  key: 'email'
                                  id: "email#{eid}"
                              }
                              button {
                                  key: 'o'
                                  onClick: @placeOrder
                              }, "Place Order"
                          ]
                      ]
                  else
                      div { key: 'so' }, "No tickets available"
            else
                null
            
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
