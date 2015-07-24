require.config
    shim:
      react:
        exports: 'React'
    paths:
        react: '../lib/react/react'
        jquery: '../lib/jquery/jquery'

# Define the requirements for this code block
require [
    'react'
    'event_list'
    ], (React, EventList) ->

    # Local reference to the React.DOM version of a div
    { div } = React.DOM

    # Define the master view class
    MasterView = React.createClass
        # Define the html / children components
        render: ->
            eventList = React.createFactory EventList
            div { key: 'top' },
                eventList { key: 'events' }, null

    # Instantiate a MasterView element
    masterView = React.createElement MasterView, null

    # Render the MasterView element inside the "mount" div in the template
    rendered = React.render masterView, document.getElementById('mount')
