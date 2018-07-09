package com.bt.rsqe.expedio.fixtures;

import com.bt.rsqe.customerrecord.CustomerDTO;

public class CustomerDTOFixture {

    public static Builder aCustomerDTO() {
        return new Builder();
    }

    public static class Builder {
        private CustomerDTO customerDTO = new CustomerDTO();

        public CustomerDTO build() {
            return customerDTO;
        }

        public Builder withId(String id) {
            customerDTO.id = id;
            return this;
        }

        public Builder withSalesChannel(String salesChannel) {
            customerDTO.salesChannel = salesChannel;
            return this;
        }

        public Builder withName(String name) {
            customerDTO.name = name;
            return this;
        }

        public Builder withGfrCode(String gfrCode) {
            customerDTO.gfrCode = gfrCode;
            return this;
        }


    }
}
