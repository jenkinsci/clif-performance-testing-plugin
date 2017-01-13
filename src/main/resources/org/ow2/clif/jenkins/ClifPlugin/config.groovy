package org.ow2.clif.jenkins

import hudson.Functions

def f=namespace(lib.FormTagLib)

f.section(title:_("Clif Plugin")) {
    f.entry(title:_("Clif root folder"), field:"clifRootDir") {
        f.textbox(default: "clif")
    }
}