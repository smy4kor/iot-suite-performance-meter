import distutils
from distutils import dir_util

import paho

# This will copy the package into the target directory (to be included in the zip file)
path = paho.__path__[0]
distutils.dir_util.copy_tree(path, "./site-packages/paho")
