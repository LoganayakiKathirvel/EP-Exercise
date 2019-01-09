/**
 * Copyright © 2014 Elastic Path Software Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
 module.exports = function (grunt) {

  grunt.initConfig({

    pkg: grunt.file.readJSON('package.json'),
    less: {
      compile: {
        files: {
          "css/css/studio.css": "css/less/studio.less"
        }
      }
    },
    copy: {
      main: {
          files: [
              {expand: true, cwd: 'node_modules/bootstrap/dist/css/', src: '*', dest: 'css/bootstrap/', flatten: true, filter: 'isFile'},
              {expand: true, cwd: 'node_modules/bootstrap/dist/fonts/', src: '*', dest: 'css/fonts/', flatten: true, filter: 'isFile'},
              {expand: true, cwd: 'node_modules/bootstrap/dist/js/', src: 'bootstrap.min.js', dest: 'js/bootstrap/', flatten: true, filter: 'isFile'},
              {expand: true, cwd: 'node_modules/codemirror/lib/', src: 'codemirror.css', dest: 'css/codemirror/', flatten: true, filter: 'isFile'},
              {expand: true, cwd: 'node_modules/codemirror/addon/dialog/', src: 'dialog.css', dest: 'css/codemirror/', flatten: true, filter: 'isFile'},
              {expand: true, cwd: 'node_modules/codemirror/addon/search/', src: '*.css', dest: 'css/codemirror/', flatten: true, filter: 'isFile'},
              {expand: true, cwd: 'node_modules/codemirror/lib/', src: 'codemirror.js', dest: 'js/codemirror/', flatten: true, filter: 'isFile'},
              {expand: true, cwd: 'node_modules/codemirror/mode/javascript/', src: 'javascript.js', dest: 'js/codemirror/', flatten: true, filter: 'isFile'},
              {expand: true, cwd: 'node_modules/codemirror/addon/dialog/', src: 'dialog.js', dest: 'js/codemirror/', flatten: true, filter: 'isFile'},
              {expand: true, cwd: 'node_modules/codemirror/addon/search/', src: '*.js', dest: 'js/codemirror/', flatten: true, filter: 'isFile'},
              {expand: true, cwd: 'node_modules/jquery/dist/', src: '*', dest: 'js/', flatten: true, filter: 'isFile'},
              {expand: true, cwd: 'node_modules/jquery.browser/dist/', src: 'jquery.browser.min.js', dest: 'js/', flatten: true, filter: 'isFile'},
              {expand: true, cwd: 'node_modules/jquery.cookie/', src: 'jquery.cookie.js', dest: 'js/', flatten: true, filter: 'isFile'},
              {expand: true, cwd: 'node_modules/jquery-bootstrap-theme/css/custom-theme/images/', src: '*', dest: 'css/img/', flatten: true, filter: 'isFile'},
              {expand: true, cwd: 'node_modules/bootstrap-select/dist/css/', src: '*', dest: 'css/bootstrap/', flatten: true, filter: 'isFile'},
              {expand: true, cwd: 'node_modules/bootstrap-select/dist/js/', src: '*', dest: 'js/bootstrap/', flatten: true, filter: 'isFile'}
          ]
      }
    },
    watch: {
      scripts: {
        files: [
        'css/less/*.less',
        'css/jq-ui-bootstrap/*.less'
        ],
        tasks: ['build']
      }
    },
    jshint: {
      all: [
      'js/api-connect.js',
      'js/studio-ui.js'
      ],
      options: {
        jshintrc: '.jshintrc',
        reporter: require('jshint-stylish')
      }
    },
    connect: {
      server: {
        options: {
          hostname: 'localhost',
          middleware: function (connect, options, defaultMiddleware) {
           var proxy = require('grunt-connect-proxy/lib/utils').proxyRequest;
           return [
                // Include the proxy first
                proxy
                ].concat(defaultMiddleware);
              }
            },
            proxies: [
            {
              context: '/cortex',
              host: 'localhost',
              port: 9080,
              https: false,
              xforward: false
            }
            ]
          }
        }
      });

  // Load the plugins for our Grunt tasks.
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-connect');
  grunt.loadNpmTasks('grunt-connect-proxy');

  grunt.registerTask('server', function (target) {
    grunt.task.run([
      'configureProxies:server',
      'connect:server',
      'watch'
      ]);
  });

  grunt.registerTask('build', function(target) {
      grunt.task.run([
          'less',
          'copy'
      ]);
  });

  // Make watch the default task
  grunt.registerTask('default', ['watch']);

};