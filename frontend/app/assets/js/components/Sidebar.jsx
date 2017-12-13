import {formatDate} from "../utils";
import {tag} from "../utils";
import {connect} from "react-redux";
import {Grid} from "react-virtualized";

let SideBarWithDetails = ({record}) => {
    return tag("div", {className: "pure-g"}, [
        tag("div", {className: "pure-u-1-2"}, Messages("details.event_date")),
        tag("div", {className: "pure-u-1-2"}, formatDate(record.eventDate)),
        tag("div", {className: "pure-u-1-2"}, Messages("details.post_date")),
        tag("div", {className: "pure-u-1-2"}, formatDate(record.publishDate)),
        tag("div", {className: "pure-u-1-2"}, Messages("details.event_type")),
        tag("div", {className: "pure-u-1-2"}, Messages("event_type." + record.eventType)),
        tag("div", {className: "pure-u-1"}, Messages("details.picture")),
        tag("div", {className: "pure-u-1"}, [
                tag("img", {className: "fullPicturePreview", src: "pictures/" + record.picture}),
                tag("div", {className: "pure-u-1"},
                    tag("a", {href: record.link}, Messages("details.link"))
                )
            ]
        )
    ]);
};

let SidebarWelcomePage = () => {
    return [
        tag("div", {
            key: "text",
            dangerouslySetInnerHTML: {__html: Messages("welcome_text")},
        }),
        tag("div", {
            key: "credits",
            className: "credits",
            dangerouslySetInnerHTML: {__html: Messages("credits")},
        }),
    ];
};

const SidebarWithRecords = /*Measure.withContentRect('bounds')*/(({records}) => {
    const cellRenderer = ({columnIndex, key, rowIndex, style}) => {
        if (rowIndex * 7 + columnIndex < records.length) {
            return tag("div", {
                key: key,
                style: style,
            }, [
                tag("img", {src: "pictures_min/" + records[rowIndex * 7 + columnIndex].picture})
            ]);
        } else {
            return null;
        }
    };

    return tag(Grid, {
        cellRenderer: cellRenderer,
        columnCount: 7,
        columnWidth: 100,
        height: 900,
        rowCount: Math.ceil(records.length / 7),
        rowHeight: 100,
        width: 750,
    });
});

const SidebarWithRecordsContainer = connect(state => {
    return {records: state.records};
})(SidebarWithRecords);

const Sidebar = ({selectedRecord}) => {
    const sidebarContents = (record) => {
        if (record) {
            return tag(SideBarWithDetails, {record: record});
        } else {
            return tag(SidebarWithRecordsContainer, {});
        }
    };

    return tag("div", {className: "sidebar-content"}, sidebarContents(selectedRecord));
};

export default Sidebar;
