package org.aion4j.maven.avm.remote;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.aion.base.util.ByteUtil;
import org.aion4j.maven.avm.exception.AVMRuntimeException;
import org.aion4j.maven.avm.signing.SignedTransactionBuilder;
import org.aion4j.maven.avm.signing.TxTool;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RemoteAVMNode {
    //test account 0xa052de3423a1e77f4c5f8500564e3601759143b7c0e652a7012d35eb67b283ca
    //0x5e444d8bf64f9f6d9022cd245341e69d8b51af793367df3894f260b958a0d72aa060fe4538c5ea49dab8ef4ac9fc24933b9b9b4ed37eb20e070e49ce0b05b6f8
    //0xa0c40d2bb3e0248b16f4f1bd5735dc22cd84b580bbc301f8cebc83e25030c6ea

    private String web3RpcUrl;

    private Log log;

    public RemoteAVMNode(String web3RpcUrl, Log log) {
        this.web3RpcUrl = web3RpcUrl;
        this.log = log;
    }

    public boolean unlock(String address, String password) {

        try {

            JSONObject jo = getJsonHeader("personal_unlockAccount");

            List<String> params = new ArrayList();
            params.add(address);
            params.add(password);
            params.add("600");

            jo.put("params", params);

            log.info("Web3Rpc request data: " + jo.toString());

            HttpResponse<JsonNode> jsonResponse = getHttpRequest()
                    .body(jo)
                    .asJson();

            JsonNode jsonNode = jsonResponse.getBody();

            if(jsonNode == null)
                return false;

            return jsonNode.getObject().getBoolean("result");
        } catch (UnirestException e) {
            throw new AVMRuntimeException("Web3Rpc call failed to unlock account", e);
        }
    }

    public String deploy(String address, String dappJarContent, long gas, long gasPrice) {
        try {

            JSONObject jo = getJsonHeader("eth_sendTransaction");
            //jo.put("id", 45);

            List<JSONObject> params = new ArrayList();

            JSONArray paramArray = new JSONArray();

            JSONObject txnJo = new JSONObject();
            txnJo.put("from", address);
            txnJo.put("gas", gas);
            txnJo.put("gasPrice", gasPrice);
            txnJo.put("type", "0xf");
            txnJo.put("data", dappJarContent);

            paramArray.put(txnJo);

            jo.put("params", paramArray);

            log.debug("Txn Object : " + jo.toString());

            HttpResponse<JsonNode> jsonResponse = getHttpRequest()
                    .body(jo)
                    .asJson();

            JsonNode jsonNode = jsonResponse.getBody();

            if(jsonNode == null)
                return null;

            log.info("Response from Aion kernel: " + jsonNode.toString());

            JSONObject jsonObject = jsonNode.getObject();

            String error = getError(jsonObject);

            if(error == null) {
                return jsonObject.optString("result");
            } else {
                throw new AVMRuntimeException("Dapp deployment failed. Reason: " + error);
            }

        } catch (UnirestException e) {
            throw new AVMRuntimeException("Dapp deployment failed", e);
        }
    }

    public String call(String contract, String address, String callData, BigInteger value, long gas, long gasPrice) {
        try {

            log.info("Invoking method of the contract ...");


            JSONObject jo = getJsonHeader("eth_call");
            //jo.put("id", 45);

            List<JSONObject> params = new ArrayList();

            JSONArray paramArray = new JSONArray();

            JSONObject txnJo = new JSONObject();
            txnJo.put("from", address);
            txnJo.put("to", contract);
            txnJo.put("gas", gas);
            txnJo.put("gasPrice", gasPrice);
           // txnJo.put("type", 0xf);
            txnJo.put("data", "0x" + callData);
            if(value != null) {
                txnJo.put("value", value);
            }

            paramArray.put(txnJo);

            jo.put("params", paramArray);

            log.info("Web3Rpc request data: " + jo.toString());

            HttpResponse<JsonNode> jsonResponse = getHttpRequest()
                    .body(jo)
                    .asJson();

            JsonNode jsonNode = jsonResponse.getBody();

            if(jsonNode == null)
                return null;

            log.info("Response from Aion kernel: " + jsonNode.toString());

            JSONObject jsonObject = jsonNode.getObject();

            String error = getError(jsonObject);

            if(error == null) {
                return jsonObject.optString("result");
            } else {
                throw new AVMRuntimeException("Contract method call failed. Reason: " + error);
            }

        } catch (UnirestException e) {
            throw new AVMRuntimeException("Contract method call failed", e);
        }
    }

    //contract transaction
    public String sendTransaction(String contract, String address, String callData, BigInteger value, long gas, long gasPrice) {
        try {

            log.info("Sending contract transaction  ...");


            JSONObject jo = getJsonHeader("eth_sendTransaction");
            //jo.put("id", 45);

            List<JSONObject> params = new ArrayList();

            JSONArray paramArray = new JSONArray();

            JSONObject txnJo = new JSONObject();
            txnJo.put("from", address);
            txnJo.put("to", contract);
            txnJo.put("gas", gas);
            txnJo.put("gasPrice", gasPrice);
            // txnJo.put("type", 0xf);
            txnJo.put("data", "0x" + callData);
            if(value != null) {
                txnJo.put("value", value);
            }

            paramArray.put(txnJo);

            jo.put("params", paramArray);

            log.info("Web3Rpc request data: " + jo.toString());

            HttpResponse<JsonNode> jsonResponse = getHttpRequest()
                    .body(jo)
                    .asJson();

            JsonNode jsonNode = jsonResponse.getBody();

            if(jsonNode == null)
                return null;

            log.info("Response from Aion kernel: " + jsonNode.toString());

            JSONObject jsonObject = jsonNode.getObject();

            String error = getError(jsonObject);

            if(error == null) {
                return jsonObject.optString("result");
            } else {
                throw new AVMRuntimeException("Contract transaction failed. Reason: " + error);
            }

        } catch (UnirestException e) {
            throw new AVMRuntimeException("Contract transaction failed", e);
        }
    }

    //contract transaction
    public String sendRawTransaction(String destination, String privateKey, String callData, BigInteger value, long gas, long gasPrice) {
        try {

            log.info("Signing transaction ...");

            String senderAddress = SignedTransactionBuilder.getAionAddress(privateKey);
            log.info("Sender address: " + senderAddress);

            String nonceHex = getTransactionCount(senderAddress);
            BigInteger nonce = BigInteger.ZERO;

            if(nonceHex != null) {
                nonce = ByteUtil.bytesToBigInteger(ByteUtil.hexStringToBytes(nonceHex));
            }

            log.info("Sender's nonce: " + nonce);

            String signedTx = null;
            try {
               // signedTx = TxTool.signWithPvtKey(privateKey, destination, value, callData, "f", nonce, gas, gasPrice );
                signedTx = SignedTransactionBuilder.signWithPvtKey(privateKey, destination, value, callData, nonce, gas, gasPrice);
            } catch (Exception e) {
                //log.error("Transaction sign failed ", e);
                throw new AVMRuntimeException("Txn signing failed ", e);
            }

            log.info("Sending raw contract transaction  ...");

            JSONObject jo = getJsonHeader("eth_sendRawTransaction");
            //jo.put("id", 45);

            if(!signedTx.startsWith("0x"))
                signedTx = "0x" + signedTx;

            List params = new ArrayList();
            params.add(signedTx);

            JSONArray paramArray = new JSONArray();

            jo.put("params", params);

            log.info("Web3Rpc request data: " + jo.toString());

            HttpResponse<JsonNode> jsonResponse = getHttpRequest()
                    .body(jo)
                    .asJson();

            JsonNode jsonNode = jsonResponse.getBody();

            if(jsonNode == null)
                return null;

            log.info("Response from Aion kernel: " + jsonNode.toString());

            JSONObject jsonObject = jsonNode.getObject();

            String error = getError(jsonObject);

            if(error == null) {
                return jsonObject.optString("result");
            } else {
                throw new AVMRuntimeException("Transaction failed. Reason: " + error);
            }

        } catch (Exception e) {
            throw new AVMRuntimeException("Transaction failed", e);
        }
    }

    /**
     * Get transaction count. Needed to get nonce of an address
     * @param address
     * @return
     */
    public String getTransactionCount(String address) {
        try {

            log.info("Get nonce ...");

            JSONObject jo = getJsonHeader("eth_getTransactionCount");
            List params = new ArrayList();
            params.add(address);
            params.add("latest");

            jo.put("params", params);

            log.info("Web3Rpc request data for getTransactionCount: " + jo.toString());

            HttpResponse<JsonNode> jsonResponse = getHttpRequest()
                    .body(jo)
                    .asJson();

            JsonNode jsonNode = jsonResponse.getBody();

            if(jsonNode == null)
                return null;

            log.info("Response from Aion kernel for getTransactionCount: " + jsonNode.toString());

            JSONObject jsonObject = jsonNode.getObject();

            String error = getError(jsonObject);

            if(error == null) {
                return jsonObject.optString("result");
            } else {
                throw new AVMRuntimeException("Getting nonce for the address failed. Reason: " + error);
            }

        } catch (Exception e) {
            throw new AVMRuntimeException("Getting nonce for the address failed. Reason: ", e);
        }
    }


    public String getBalance(String address) {

        try {

            JSONObject jo = getJsonHeader("eth_getBalance");

            List<String> params = new ArrayList();
            params.add(address);
            params.add("latest");

            jo.put("params", params);

            log.info("Web3Rpc request data: " + jo.toString());

            HttpResponse<JsonNode> jsonResponse = getHttpRequest()
                    .body(jo)
                    .asJson();

            JsonNode jsonNode = jsonResponse.getBody();

            if(log.isDebugEnabled())
                log.debug("Get status code >>> " + jsonResponse.getStatus());

            if(jsonNode == null)
                return null;

            log.info("Response from Aion kernel: " + jsonNode.toString());

            JSONObject jsonObject = jsonNode.getObject();

            String error = getError(jsonObject);

            if(error == null) {
                return jsonObject.optString("result");
            } else {
                throw new AVMRuntimeException("get-balance failed. Reason: " + error);
            }

        } catch (UnirestException e) {
            throw new AVMRuntimeException("Web3Rpc call failed for get balance", e);
        }
    }

    public String transfer(String from, String to, BigInteger value, long gas, long gasPrice) {

        try {
            String nonceHex = getTransactionCount(from);
            BigInteger nonce = BigInteger.ZERO;

            if(nonceHex != null) {
                nonce = ByteUtil.bytesToBigInteger(ByteUtil.hexStringToBytes(nonceHex));
            }

            log.info("Sender's nonce: " + nonce);

            log.info("Sending transfer transaction  ...");

            JSONObject jo = getJsonHeader("eth_sendTransaction");
            //jo.put("id", 45);

            List<JSONObject> params = new ArrayList();

            JSONArray paramArray = new JSONArray();

            JSONObject txnJo = new JSONObject();
            txnJo.put("from", from);
            txnJo.put("to", to);
            txnJo.put("value", value);
            txnJo.put("gas", gas);
            txnJo.put("gasPrice", gasPrice);
            txnJo.put("nonce", nonceHex);

            txnJo.put("data", "");

            paramArray.put(txnJo);

            jo.put("params", paramArray);

            log.info("Web3Rpc request data: " + jo.toString());

            HttpResponse<JsonNode> jsonResponse = getHttpRequest()
                    .body(jo)
                    .asJson();

            JsonNode jsonNode = jsonResponse.getBody();

            if(jsonNode == null)
                return null;

            log.info("Response from Aion kernel: " + jsonNode.toString());

            JSONObject jsonObject = jsonNode.getObject();

            String error = getError(jsonObject);

            if(error == null) {
                return jsonObject.optString("result");
            } else {
                throw new AVMRuntimeException("Transfer transaction failed. Reason: " + error);
            }

        } catch (UnirestException e) {
            throw new AVMRuntimeException("Transfer transaction failed", e);
        }
    }

    public JSONObject getReceipt(String txHash) {

        try {

            JSONObject jo = getJsonHeader("eth_getTransactionReceipt");

            List<String> params = new ArrayList();
            params.add(txHash);

            jo.put("params", params);

            log.info("Web3Rpc request data: " + jo.toString());

            HttpResponse<JsonNode> jsonResponse = getHttpRequest()
                    .body(jo)
                    .asJson();

            JsonNode jsonNode = jsonResponse.getBody();

            if(jsonNode == null)
                return null;

            log.info("Response from Aion kernel: " + jsonNode.toString());

            JSONObject jsonObject = jsonNode.getObject();

            String error = getError(jsonObject);

            if(error == null) {
                return jsonObject;//jsonObject.optString("result");
            } else {
                throw new AVMRuntimeException("getRecipt call failed. Reason: " + error);
            }

        } catch (UnirestException e) {
            throw new AVMRuntimeException("getReceipt call failed", e);
        }
    }

    public String createAccount(String password) {

        try {

            JSONObject jo = getJsonHeader("personal_newAccount");

            List<String> params = new ArrayList();
            params.add(password);

            jo.put("params", params);

            log.info("Web3Rpc request data: " + jo.toString());

            HttpResponse<JsonNode> jsonResponse = getHttpRequest()
                    .body(jo)
                    .asJson();

            JsonNode jsonNode = jsonResponse.getBody();

            if(jsonNode == null)
                return null;

            log.info("Response from Aion kernel: " + jsonNode.toString());

            JSONObject jsonObject = jsonNode.getObject();

            String error = getError(jsonObject);

            if(error == null) {
                return jsonObject.optString("result");
            } else {
                throw new AVMRuntimeException("New account creation failed: " + error);
            }

        } catch (UnirestException e) {
            throw new AVMRuntimeException("New account creation failed", e);
        }
    }

    public String getLogs(String fromBlock, String toBlock, String addresses, String topics, String blockHash) {
        try {

            log.info("Getting logs");

            JSONObject jo = getJsonHeader("eth_getLogs");
            //jo.put("id", 45);

            List<JSONObject> params = new ArrayList();


            JSONObject filters = new JSONObject();

            if(fromBlock != null && !fromBlock.trim().isEmpty())
                filters.put("fromBlock", fromBlock);

            if(toBlock != null && !toBlock.trim().isEmpty())
                filters.put("toBlock", toBlock);

            if(addresses != null && !addresses.trim().isEmpty()) {
                //split addresses
                String[] addArray = addresses.split(",");

                JSONArray jsonArray = new JSONArray();
                for(String address: addArray) {
                    jsonArray.put(address.trim());
                }

                filters.put("address", jsonArray);
            }

            if(topics != null && !topics.trim().isEmpty()) {
                //split topics
                String[] topicsArray = topics.split(",");

                JSONArray jsonArray = new JSONArray();
                for(String topic: topicsArray) {
                    jsonArray.put(topic);
                }

                filters.put("topics", jsonArray);
            }

            if(blockHash != null && !blockHash.isEmpty()) {
                filters.put("blockhash", blockHash);

            }

            params.add(filters);


            jo.put("params", params);

            log.info("Web3Rpc request data: " + jo.toString());

            HttpResponse<JsonNode> jsonResponse = getHttpRequest()
                    .body(jo)
                    .asJson();

            JsonNode jsonNode = jsonResponse.getBody();

            if(jsonNode == null)
                return null;

            log.info("Response from Aion kernel: " + jsonNode.toString());

            JSONObject jsonObject = jsonNode.getObject();

            String error = getError(jsonObject);

            if(error == null) {
                return jsonObject.toString();
            } else {
                throw new AVMRuntimeException("getLogs() failed. Reason: " + error);
            }

        } catch (UnirestException e) {
            throw new AVMRuntimeException("getLogs() failed", e);
        }
    }


    private String getError(JSONObject jsonObject) {
        JSONObject error = jsonObject.optJSONObject("error");

        if(error != null)
            return error.toString();
        else
            return null;
    }


    public String getLatestBlock() {

        try {

            JSONObject jo = getJsonHeader("eth_blocknumber");
            jo.put("params", Collections.EMPTY_LIST);
            jo.put("id", 42);

            HttpResponse<JsonNode> jsonResponse = getHttpRequest()
                    .body(jo)
                    .asJson();

            JsonNode jsonNode = jsonResponse.getBody();

            Object blockNumber = jsonNode.getObject().get("result");

            return blockNumber != null? blockNumber.toString(): null;
        } catch (UnirestException e) {
            throw new AVMRuntimeException("Web3Rpc call failed for unlock account", e);
        }
    }

    private JSONObject getJsonHeader(String method) {
        JSONObject jo = new JSONObject();
        jo.put("jsonrpc", "2.0");
        jo.put("method", method);
        return jo;
    }


    private HttpRequestWithBody getHttpRequest() {
        return Unirest.post(web3RpcUrl)
                .header("accept", "application/json")
                .header("Content-Type", "application/json");

        //.queryString("apiKey", "123")
    }
}
