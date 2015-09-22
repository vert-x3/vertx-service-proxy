/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/** @module test-js/test_connection */

/**

 @class
*/
var TestConnection = function(eb, address) {

  var j_eb = eb;
  var j_address = address;
  var closed = false;
  var that = this;
  var convCharCollection = function(coll) {
    var ret = [];
    for (var i = 0;i < coll.length;i++) {
      ret.push(String.fromCharCode(coll[i]));
    }
    return ret;
  };

  /**

   @public
   @param resultHandler {function} 
   @return {TestConnection}
   */
  this.startTransaction = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      if (closed) {
        throw new Error('Proxy is closed');
      }
      j_eb.send(j_address, {}, {"action":"startTransaction"}, function(result) { __args[0](result.body); }, function(failure) { __args[0](null, failure); });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param name {string} 
   @param data {Object} 
   @param resultHandler {function} 
   @return {TestConnection}
   */
  this.insert = function(name, data, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && typeof __args[1] === 'object' && typeof __args[2] === 'function') {
      if (closed) {
        throw new Error('Proxy is closed');
      }
      j_eb.send(j_address, {"name":__args[0], "data":__args[1]}, {"action":"insert"}, function(result) { __args[2](result.body); }, function(failure) { __args[2](null, failure); });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   @return {TestConnection}
   */
  this.commit = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      if (closed) {
        throw new Error('Proxy is closed');
      }
      j_eb.send(j_address, {}, {"action":"commit"}, function(result) { __args[0](result.body); }, function(failure) { __args[0](null, failure); });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param resultHandler {function} 
   @return {TestConnection}
   */
  this.rollback = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      if (closed) {
        throw new Error('Proxy is closed');
      }
      j_eb.send(j_address, {}, {"action":"rollback"}, function(result) { __args[0](result.body); }, function(failure) { __args[0](null, failure); });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   */
  this.close = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (closed) {
        throw new Error('Proxy is closed');
      }
      j_eb.send(j_address, {}, {"action":"close"});
      closed = true;
      return;
    } else throw new TypeError('function invoked with invalid arguments');
  };

};

// We export the Constructor function
module.exports = TestConnection;